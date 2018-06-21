<?xml version="1.0" encoding="UTF-8"?>

<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform"
                xmlns:xsd="http://www.w3.org/2001/XMLSchema"
                version="2.0">
    
    <xsl:output method="text"/>

    <xsl:template match="/">
        
        <xsl:apply-templates />

    </xsl:template>

    <xsl:template match="xsd:documentation">
        <xsl:value-of select="." />
    </xsl:template>
    
    <xsl:template match="xsd:element">
        
        <xsl:value-of select="@name" />
        
        <xsl:apply-templates select="xsd:annotation/xsd:documentation" />
        
    </xsl:template>
    
</xsl:stylesheet>
