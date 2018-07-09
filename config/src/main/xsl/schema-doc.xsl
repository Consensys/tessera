<?xml version="1.0" encoding="UTF-8"?>
<xsl:stylesheet xmlns:xsl="http://www.w3.org/1999/XSL/Transform" xmlns:xsd="http://www.w3.org/2001/XMLSchema" version="2.0">
    <xsl:output indent="yes" method="html" omit-xml-declaration="yes" />

    <xsl:template match="/">
        <html>
            <head>
                <title>Entities</title>
                <link rel="stylesheet" href="https://maxcdn.bootstrapcdn.com/bootstrap/3.3.4/css/bootstrap.min.css" />
            </head>
            <body>
                <div class="container">
                    <xsl:apply-templates />
                </div>
            </body>
        </html>
    </xsl:template>

    <xsl:template match="xsd:element">
        <tr>
            <td>
                <xsl:value-of select="@name" />
            </td>
            <td>
                <xsl:value-of select="@type" />
            </td>
            <td>
                <xsl:choose>
                    <xsl:when test="@minOccurs">
                        <xsl:value-of select="@minOccurs" />
                    </xsl:when>
                    <xsl:otherwise>
                        1
                    </xsl:otherwise>
                </xsl:choose>
                ..
                <xsl:choose>
                    <xsl:when test="@maxOccurs">
                        <xsl:value-of select="@maxOccurs" />
                    </xsl:when>
                    <xsl:otherwise>
                        1
                    </xsl:otherwise>
                </xsl:choose>
            </td>
            <td>
                <xsl:value-of select="xsd:annotation/xsd:documentation" />
            </td>
        </tr>
    </xsl:template>

    <xsl:template match="xsd:complexType">
        <h2>
            <xsl:value-of select="@name"></xsl:value-of>
        </h2>
        <p>
            <xsl:value-of select="xsd:annotation/xsd:documentation" />
        </p>
        <table class="table table-striped table-bordered table-hover">
            <tr>
                <th>Name</th>
                <th>Type</th>
                <th>Cardinality</th>
                <th>Description</th>
            </tr>

            <xsl:apply-templates select="xsd:sequence|xsd:complexContent|xsd:choice|xsd:all" />
        </table>
    </xsl:template>

</xsl:stylesheet>