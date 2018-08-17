<?xml version="1.0"?>
<xsl:stylesheet version="2.0" xmlns:xsl="http://www.w3.org/1999/XSL/Transform" 
                xmlns="http://tessera.github.com/config">
    
    <xsl:output method="xml" indent="yes" encoding="UTF-8"/>

    <xsl:template match="@*|node()">
        <xsl:copy>
            <xsl:apply-templates select="@*|node()"/>
        </xsl:copy>
    </xsl:template>

    <xsl:template match="*[local-name() = 'password']">
        <xsl:element name="{local-name()}">
            <xsl:text>*********</xsl:text>
        </xsl:element>
    </xsl:template>
    
    
    <xsl:template match="*[contains(local-name(),'Password')]">
        <xsl:element name="{local-name()}">
            <xsl:text>*********</xsl:text>
        </xsl:element>
    </xsl:template>


    <xsl:template match="*[local-name() = 'privateKey']">
        
        <xsl:if test="not(../*[local-name() = 'privateKeyPath'])">
            <xsl:element name="{local-name()}">
                <xsl:text>*********</xsl:text>
            </xsl:element>
        </xsl:if>
        
    </xsl:template>

</xsl:stylesheet>