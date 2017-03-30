<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns="http://graphml.graphdrawing.org/xmlns"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform" version="2.0">
    
    <xsl:import href="output_layout.xslt"/>

    <xsl:output indent="yes" method="xml"/>

    <xsl:template match="text()"/>

    <!-- writes the layout in a file that has the same name as the target document,
        except with .layout extension. -->
    <xsl:param name="path"/>
    <xsl:variable name="file" select="replace($path, '(.+)[.].+', '$1.layout')"/>

    <!-- Top-level: graph -> graph -->
    <xsl:template match="graph">
        
        <!-- layout information -->
        <xsl:result-document href="file:///{$file}" method="xml" indent="yes">
            <xsl:call-template name="setLayout"/>
        </xsl:result-document>

        <!-- graph -->
        <graphml>
			  <key attr.name="arguments" for="node" id="arguments"/>
   			<key attr.name="parameters" for="graph" id="parameters"/>
  			<key attr.name="variables" for="graph" id="variables"/>
   			<key attr.name="name" attr.type="string" for="graph"/>
			  <key attr.name="model" attr.type="string" for="graph"/>
    		<key attr.name="name" attr.type="string" for="node"/>
    		<key attr.name="kind" attr.type="string" for="node"/>
   			<key attr.name="port_direction" attr.type="string" for="node"/>
    		<key attr.name="graph_desc" attr.type="string" for="node"/>
    		<key attr.name="nbRepeat" attr.type="int" for="node"/>
        <key attr.name="memory_script" attr.type="string" for="node"/>  
    		<key attr.name="edge_cons" attr.type="string" for="edge"/>
    		<key attr.name="edge_delay" attr.type="string" for="edge"/>
    		<key attr.name="edge_prod" attr.type="string" for="edge"/>
        <key attr.name="target_port_modifier" attr.type="string" for="edge"/>
        <key attr.name="source_port_modifier" attr.type="string" for="edge"/>
    		<key attr.name="data_type" attr.type="string" for="edge"/>
            <graph edgedefault="directed">
                <data key="name">
                	<xsl:call-template name="fileName">
       					<xsl:with-param name="path" select="$path" />
      				</xsl:call-template>
                </data>
                <data key="kind">
                    <xsl:value-of select="parameters/parameter[@name = 'kind']/@value"/>
                </data>
                <xsl:apply-templates select="parameters/parameter[@name = 'graph parameter']"/>
                <xsl:apply-templates select="parameters/parameter[@name = 'graph variable']"/>

                <xsl:apply-templates select="vertices/vertex"/>
                <xsl:apply-templates select="edges/edge"/>
            </graph>
        </graphml>
    </xsl:template>

    <!-- Parameter declarations -->
    <xsl:template match="parameter[@name = 'graph parameter']">
            <data key="parameters">
                <xsl:apply-templates select="element"/>
            </data>
    </xsl:template>

    <!-- Variable declarations -->
    <xsl:template match="parameter[@name = 'graph variable']">
        <data key="variables">
            <xsl:apply-templates select="entry" mode="variable"/>
        </data>
    </xsl:template>

    <!-- node -->
    <xsl:template match="vertex">
        <node id="{parameters/parameter[@name = 'id']/@value}">
            <data key="name">
                <xsl:value-of select="parameters/parameter[@name = 'id']/@value"/>
            </data>
            <data key="kind">
                <xsl:value-of select="parameters/parameter[@name = 'kind']/@value"/>
            </data>
            <data key="graph_desc">
                <xsl:value-of select="parameters/parameter[@name = 'refinement']/@value"/>
            </data>

          <xsl:if test="parameters/parameter[@name = 'memory script']/@value">
            <data key="memory_script">
              <xsl:value-of select="parameters/parameter[@name = 'memory script']/@value"/>
            </data>
          </xsl:if>  

            <xsl:apply-templates select="parameters/parameter[@name = 'instance argument']"/>
			
			<xsl:choose>
				<xsl:when test="@type= 'Input port'">
					<data key="port_direction">Input</data>
				</xsl:when>
				<xsl:when test="@type= 'Output port'">
					<data key="port_direction">Output</data>
				</xsl:when>
				<xsl:otherwise></xsl:otherwise>
			</xsl:choose>

        </node>
    </xsl:template>
    
    <!-- node parameter -->
    <xsl:template match="parameter[@name = 'instance argument']">
        <data key="arguments">
            <xsl:apply-templates select="entry" mode="argument"/>
        </data>
    </xsl:template>
    
    <!-- arguments argument -->
    <xsl:template match="entry" mode="argument">
        <argument name="{@key}" value="{@value}"/>
    </xsl:template>
    
    <!-- variables variable -->
    <xsl:template match="entry" mode="variable">
        <variable name="{@key}" value="{@value}"/>
    </xsl:template>
 
    
    <!-- parameters parameter -->
    <xsl:template match="element">
        <parameter name="{@value}"/>
    </xsl:template>

    <!-- edge -->
    <xsl:template match="edge">
        <edge source="{@source}" target="{@target}"
            sourceport="{parameters/parameter[@name = 'source port']/@value}"
            targetport="{parameters/parameter[@name = 'target port']/@value}">
            <data key="edge_prod">
                <xsl:value-of select="parameters/parameter[@name = 'source production']/@value"/>
            </data>

            <data key="edge_delay">
                <xsl:value-of select="parameters/parameter[@name = 'delay']/@value"/>
            </data>

            <data key="edge_cons">
                <xsl:value-of select="parameters/parameter[@name = 'target consumption']/@value"/>
            </data>
            
            <data key="data_type">
                <xsl:variable name="dataType" select="parameters/parameter[@name = 'data type']/@value"/>
                <xsl:value-of select="$dataType"/>
            </data>
			
			<xsl:if test="parameters/parameter[@name = 'target port modifier']/@value">
			<data key="target_port_modifier">
                <xsl:value-of select="parameters/parameter[@name = 'target port modifier']/@value"/>
            </data>
			</xsl:if>
			
			<xsl:if test="parameters/parameter[@name = 'source port modifier']/@value">
			<data key="source_port_modifier">
                <xsl:value-of select="parameters/parameter[@name = 'source port modifier']/@value"/>
            </data>
			</xsl:if>
        </edge>
    </xsl:template>
    
    
    <xsl:template name="fileName">
  		<xsl:param name="path" />
 			<xsl:choose>
    			<xsl:when test="contains($path,'\')">
     				<xsl:call-template name="fileName">
       					<xsl:with-param name="path" select="substring-after($path,'\')" />
      				</xsl:call-template>
   				 </xsl:when>
    			 <xsl:when test="contains($path,'/')">
      				<xsl:call-template name="fileName">
        				<xsl:with-param name="path" select="substring-after($path,'/')" />
      				</xsl:call-template>
    			 </xsl:when>
    			 <xsl:otherwise>
      				<xsl:value-of select="substring-before($path,'.')" />
    			 </xsl:otherwise>
  		</xsl:choose>
	</xsl:template>

</xsl:stylesheet>
