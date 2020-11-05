#
# Copyright or © or Copr. IETR/INSA - Rennes (%%DATE%%) :
#
# %%AUTHORS%%
#
# This software is a computer program whose purpose is to help prototyping
# parallel applications using dataflow formalism.
#
# This software is governed by the CeCILL  license under French law and
# abiding by the rules of distribution of free software.  You can  use,
# modify and/ or redistribute the software under the terms of the CeCILL
# license as circulated by CEA, CNRS and INRIA at the following URL
# "http://www.cecill.info".
#
# As a counterpart to the access to the source code and  rights to copy,
# modify and redistribute granted by the license, users are provided only
# with a limited warranty  and the software's author,  the holder of the
# economic rights,  and the successive licensors  have only  limited
# liability.
#
# In this respect, the user's attention is drawn to the risks associated
# with loading,  using,  modifying and/or developing or reproducing the
# software by the user in light of its specific status of free software,
# that may mean  that it is complicated to manipulate,  and  that  also
# therefore means  that it is reserved for developers  and  experienced
# professionals having in-depth computer knowledge. Users are therefore
# encouraged to load and test the software's suitability as regards their
# requirements in conditions enabling the security of their systems and/or
# data to be ensured and,  more generally, to use and operate it in the
# same conditions as regards security.
#
# The fact that you are presently reading this means that you have had
# knowledge of the CeCILL license and that you accept its terms.
#

import git, re, os, subprocess
import shutil
from tempfile import mkstemp
from progress.bar import IncrementalBar

Comments = {
    ".java": ["/*", " */", " *"],
    ".xtend": ["/*", " */", " *"],
    ".xcore": ["/*", " */", " *"],
    ".xml": ["<!--", " -->", "   "],
    ".html": ["<!--", " -->", "   "],
    ".exsd": ["<!--", " -->", "   "],
    ".properties": ["#", "", "#"],
}

def getHeaderRangeFromCommit(repo, hash, diff, extension):
    filename = re.findall(r'b\/([\w.\/\-]*)', diff)[0]
    fileAtCommit = repo.git.show(hash + ":" + filename)

    start = -1
    valid = False
    for index, line in enumerate(fileAtCommit.splitlines()):
        if start == -1:
            if line.contains(Comments[extension][0]):
                start = index
        else:
            if line.contains("Copyright"):
                valid = True
            if line.contains(Comments[extension][1]):
                if valid:
                    return start, index
                else:
                    start = -1
            elif line.contains(Comments[extension][2]):
                continue
            else:
                return 0, 0
    return 0, 0

def isHeaderOnlyCommit(diff, headerRange):
    rangeDiffs = re.findall(r'@@ -(\d*)(,(\d))? \+(\d*)(,(\d))? @@', diff, re.MULTILINE)

    for rangeDiff in rangeDiffs:
        start = int(rangeDiff[3])
        end = int(rangeDiff[3]) if rangeDiff[4] == "" else int(rangeDiff[3]) + int(rangeDiff[5])
        if start < headerRange[0] or end > headerRange[1]:
            return False
    return True

def replaceFile(filename, fileDate, authors):
    fileDateStr = str(fileDate[0]) if fileDate[0] == fileDate[1] else str(fileDate[0]) + " - " + str(fileDate[1])
    authorsStr = ""
    _, ext = os.path.splitext(filename)

    nameSorted = sorted(authors.keys())
    for email in nameSorted:
        author = authors[email]
        if author["date"][0] == author["date"][1]:
            authorsStr = authorsStr + "\\n" + Comments[ext][2] + " " + author["name"] + " [" + email + "] (" + author["date"][0] + ")"
        else:
            authorsStr = authorsStr + "\\n" + Comments[ext][2] + " " + author["name"] + " [" + email + "] (" + author["date"][0] + " - " + author["date"][1] + ")"
    if authorsStr != "":
        authorsStr = authorsStr[(3+len(Comments[ext][2])):] + "\\n %%DEL%%"

    subprocess.call('sed -i -e \'s$%%DATE%%$' + fileDateStr + '$g\' ' + filename, shell=True)
    subprocess.call('sed -i -e \'s$%%AUTHORS%%$' + authorsStr + '$g\' ' + filename, shell=True)
    subprocess.call('sed -i /%%DEL%%/d ' + filename, shell=True)

if __name__ == '__main__':
    repo = git.Repo("../..")

    files = []
    popen = subprocess.Popen(["git", "ls-tree", "--name-only", "-r", "HEAD"], cwd="../..", stdout=subprocess.PIPE, stderr=None, encoding="utf-8")
    for file in popen.communicate()[0].splitlines():
        _, ext = os.path.splitext(file)
        if ext in Comments.keys():
            with open("../../" + file) as f:
                for lineF in f:
                    if "%%DATE%%" in lineF:
                        files.append(str(file))
                        break

    bar = IncrementalBar('Countdown', max=len(files))

    for file in files:
        filelog = repo.git.log("-p", "-U0", "--follow", "--use-mailmap", "--date=format:%Y", "--format=## %ad %aN <%aE> %H ##", file)

        commitLogs = re.findall(r'^## (\d{4}) ([\w ]*) <([^>]*)> (\w*) ##\n\n([^##]*)', filelog, re.MULTILINE)

        fileDate = -1
        authors = {}
        for commitLog in commitLogs:
            year = commitLog[0]
            name = commitLog[1]
            email = commitLog[2]
            hash = commitLog[3]
            diff = commitLog[4]

            createdFile = not(len(re.findall(r'--- \/dev\/null', diff, re.MULTILINE)) == 0)
            deletedFile = not(len(re.findall(r'\+\+\+ \/dev\/null', diff, re.MULTILINE)) == 0)

            if (not deletedFile) and (createdFile or not isHeaderOnlyCommit(diff, getHeaderRangeFromCommit(repo, hash, diff))):
                # Update file date
                if fileDate == -1:
                    fileDate = (year, year)
                else:
                    fileDate = (min(year, fileDate[0]), max(year, fileDate[1]))

                # Update Author date
                if email not in authors.keys():
                    authors[email] = {}
                    authors[email]["date"] = (year, year)
                    authors[email]["name"] = name
                else:
                    authors[email]["date"] = (min(year, authors[email]["date"][0]), max(year, authors[email]["date"][1]))

        replaceFile("../../" + file, fileDate, authors)
        bar.next()
    bar.finish()
