/**
 * DocumentDataReturn.java
 *
 * This file was auto-generated from WSDL
 * by the Apache Axis 1.4 Apr 22, 2006 (06:55:48 PDT) WSDL2Java emitter.
 */

package br.andrew.sap.document;

public class DocumentDataReturn  implements java.io.Serializable {
    private java.lang.String IDDOCUMENT;

    private java.lang.String NMTITLE;

    private java.lang.String IDCATEGORY;

    private java.lang.String NMCATEGORY;

    private java.lang.String STATUS;

    private int NRHITS;

    private java.lang.String NMAUTHOR;

    private java.lang.String IDREVISION;

    private java.lang.String DTDOCUMENT;

    private int QTVALIDITY;

    private java.lang.String FGMEASVALID;

    private java.lang.String REVALIDATION;

    private java.lang.String DTVALIDITY;

    private java.lang.String RESUME;

    private java.lang.String CANCELREASON;

    private java.lang.String URL;

    private java.lang.String LINKSHARE;

    private br.andrew.sap.document.Attributtes[] ATTRIBUTTES;

    private br.andrew.sap.document.ElectronicFile[] ELECTRONICFILE;

    private java.lang.String ERROR;

    public DocumentDataReturn() {
    }

    public DocumentDataReturn(
           java.lang.String IDDOCUMENT,
           java.lang.String NMTITLE,
           java.lang.String IDCATEGORY,
           java.lang.String NMCATEGORY,
           java.lang.String STATUS,
           int NRHITS,
           java.lang.String NMAUTHOR,
           java.lang.String IDREVISION,
           java.lang.String DTDOCUMENT,
           int QTVALIDITY,
           java.lang.String FGMEASVALID,
           java.lang.String REVALIDATION,
           java.lang.String DTVALIDITY,
           java.lang.String RESUME,
           java.lang.String CANCELREASON,
           java.lang.String URL,
           java.lang.String LINKSHARE,
           br.andrew.sap.document.Attributtes[] ATTRIBUTTES,
           br.andrew.sap.document.ElectronicFile[] ELECTRONICFILE,
           java.lang.String ERROR) {
           this.IDDOCUMENT = IDDOCUMENT;
           this.NMTITLE = NMTITLE;
           this.IDCATEGORY = IDCATEGORY;
           this.NMCATEGORY = NMCATEGORY;
           this.STATUS = STATUS;
           this.NRHITS = NRHITS;
           this.NMAUTHOR = NMAUTHOR;
           this.IDREVISION = IDREVISION;
           this.DTDOCUMENT = DTDOCUMENT;
           this.QTVALIDITY = QTVALIDITY;
           this.FGMEASVALID = FGMEASVALID;
           this.REVALIDATION = REVALIDATION;
           this.DTVALIDITY = DTVALIDITY;
           this.RESUME = RESUME;
           this.CANCELREASON = CANCELREASON;
           this.URL = URL;
           this.LINKSHARE = LINKSHARE;
           this.ATTRIBUTTES = ATTRIBUTTES;
           this.ELECTRONICFILE = ELECTRONICFILE;
           this.ERROR = ERROR;
    }


    /**
     * Gets the IDDOCUMENT value for this DocumentDataReturn.
     * 
     * @return IDDOCUMENT
     */
    public java.lang.String getIDDOCUMENT() {
        return IDDOCUMENT;
    }


    /**
     * Sets the IDDOCUMENT value for this DocumentDataReturn.
     * 
     * @param IDDOCUMENT
     */
    public void setIDDOCUMENT(java.lang.String IDDOCUMENT) {
        this.IDDOCUMENT = IDDOCUMENT;
    }


    /**
     * Gets the NMTITLE value for this DocumentDataReturn.
     * 
     * @return NMTITLE
     */
    public java.lang.String getNMTITLE() {
        return NMTITLE;
    }


    /**
     * Sets the NMTITLE value for this DocumentDataReturn.
     * 
     * @param NMTITLE
     */
    public void setNMTITLE(java.lang.String NMTITLE) {
        this.NMTITLE = NMTITLE;
    }


    /**
     * Gets the IDCATEGORY value for this DocumentDataReturn.
     * 
     * @return IDCATEGORY
     */
    public java.lang.String getIDCATEGORY() {
        return IDCATEGORY;
    }


    /**
     * Sets the IDCATEGORY value for this DocumentDataReturn.
     * 
     * @param IDCATEGORY
     */
    public void setIDCATEGORY(java.lang.String IDCATEGORY) {
        this.IDCATEGORY = IDCATEGORY;
    }


    /**
     * Gets the NMCATEGORY value for this DocumentDataReturn.
     * 
     * @return NMCATEGORY
     */
    public java.lang.String getNMCATEGORY() {
        return NMCATEGORY;
    }


    /**
     * Sets the NMCATEGORY value for this DocumentDataReturn.
     * 
     * @param NMCATEGORY
     */
    public void setNMCATEGORY(java.lang.String NMCATEGORY) {
        this.NMCATEGORY = NMCATEGORY;
    }


    /**
     * Gets the STATUS value for this DocumentDataReturn.
     * 
     * @return STATUS
     */
    public java.lang.String getSTATUS() {
        return STATUS;
    }


    /**
     * Sets the STATUS value for this DocumentDataReturn.
     * 
     * @param STATUS
     */
    public void setSTATUS(java.lang.String STATUS) {
        this.STATUS = STATUS;
    }


    /**
     * Gets the NRHITS value for this DocumentDataReturn.
     * 
     * @return NRHITS
     */
    public int getNRHITS() {
        return NRHITS;
    }


    /**
     * Sets the NRHITS value for this DocumentDataReturn.
     * 
     * @param NRHITS
     */
    public void setNRHITS(int NRHITS) {
        this.NRHITS = NRHITS;
    }


    /**
     * Gets the NMAUTHOR value for this DocumentDataReturn.
     * 
     * @return NMAUTHOR
     */
    public java.lang.String getNMAUTHOR() {
        return NMAUTHOR;
    }


    /**
     * Sets the NMAUTHOR value for this DocumentDataReturn.
     * 
     * @param NMAUTHOR
     */
    public void setNMAUTHOR(java.lang.String NMAUTHOR) {
        this.NMAUTHOR = NMAUTHOR;
    }


    /**
     * Gets the IDREVISION value for this DocumentDataReturn.
     * 
     * @return IDREVISION
     */
    public java.lang.String getIDREVISION() {
        return IDREVISION;
    }


    /**
     * Sets the IDREVISION value for this DocumentDataReturn.
     * 
     * @param IDREVISION
     */
    public void setIDREVISION(java.lang.String IDREVISION) {
        this.IDREVISION = IDREVISION;
    }


    /**
     * Gets the DTDOCUMENT value for this DocumentDataReturn.
     * 
     * @return DTDOCUMENT
     */
    public java.lang.String getDTDOCUMENT() {
        return DTDOCUMENT;
    }


    /**
     * Sets the DTDOCUMENT value for this DocumentDataReturn.
     * 
     * @param DTDOCUMENT
     */
    public void setDTDOCUMENT(java.lang.String DTDOCUMENT) {
        this.DTDOCUMENT = DTDOCUMENT;
    }


    /**
     * Gets the QTVALIDITY value for this DocumentDataReturn.
     * 
     * @return QTVALIDITY
     */
    public int getQTVALIDITY() {
        return QTVALIDITY;
    }


    /**
     * Sets the QTVALIDITY value for this DocumentDataReturn.
     * 
     * @param QTVALIDITY
     */
    public void setQTVALIDITY(int QTVALIDITY) {
        this.QTVALIDITY = QTVALIDITY;
    }


    /**
     * Gets the FGMEASVALID value for this DocumentDataReturn.
     * 
     * @return FGMEASVALID
     */
    public java.lang.String getFGMEASVALID() {
        return FGMEASVALID;
    }


    /**
     * Sets the FGMEASVALID value for this DocumentDataReturn.
     * 
     * @param FGMEASVALID
     */
    public void setFGMEASVALID(java.lang.String FGMEASVALID) {
        this.FGMEASVALID = FGMEASVALID;
    }


    /**
     * Gets the REVALIDATION value for this DocumentDataReturn.
     * 
     * @return REVALIDATION
     */
    public java.lang.String getREVALIDATION() {
        return REVALIDATION;
    }


    /**
     * Sets the REVALIDATION value for this DocumentDataReturn.
     * 
     * @param REVALIDATION
     */
    public void setREVALIDATION(java.lang.String REVALIDATION) {
        this.REVALIDATION = REVALIDATION;
    }


    /**
     * Gets the DTVALIDITY value for this DocumentDataReturn.
     * 
     * @return DTVALIDITY
     */
    public java.lang.String getDTVALIDITY() {
        return DTVALIDITY;
    }


    /**
     * Sets the DTVALIDITY value for this DocumentDataReturn.
     * 
     * @param DTVALIDITY
     */
    public void setDTVALIDITY(java.lang.String DTVALIDITY) {
        this.DTVALIDITY = DTVALIDITY;
    }


    /**
     * Gets the RESUME value for this DocumentDataReturn.
     * 
     * @return RESUME
     */
    public java.lang.String getRESUME() {
        return RESUME;
    }


    /**
     * Sets the RESUME value for this DocumentDataReturn.
     * 
     * @param RESUME
     */
    public void setRESUME(java.lang.String RESUME) {
        this.RESUME = RESUME;
    }


    /**
     * Gets the CANCELREASON value for this DocumentDataReturn.
     * 
     * @return CANCELREASON
     */
    public java.lang.String getCANCELREASON() {
        return CANCELREASON;
    }


    /**
     * Sets the CANCELREASON value for this DocumentDataReturn.
     * 
     * @param CANCELREASON
     */
    public void setCANCELREASON(java.lang.String CANCELREASON) {
        this.CANCELREASON = CANCELREASON;
    }


    /**
     * Gets the URL value for this DocumentDataReturn.
     * 
     * @return URL
     */
    public java.lang.String getURL() {
        return URL;
    }


    /**
     * Sets the URL value for this DocumentDataReturn.
     * 
     * @param URL
     */
    public void setURL(java.lang.String URL) {
        this.URL = URL;
    }


    /**
     * Gets the LINKSHARE value for this DocumentDataReturn.
     * 
     * @return LINKSHARE
     */
    public java.lang.String getLINKSHARE() {
        return LINKSHARE;
    }


    /**
     * Sets the LINKSHARE value for this DocumentDataReturn.
     * 
     * @param LINKSHARE
     */
    public void setLINKSHARE(java.lang.String LINKSHARE) {
        this.LINKSHARE = LINKSHARE;
    }


    /**
     * Gets the ATTRIBUTTES value for this DocumentDataReturn.
     * 
     * @return ATTRIBUTTES
     */
    public br.andrew.sap.document.Attributtes[] getATTRIBUTTES() {
        return ATTRIBUTTES;
    }


    /**
     * Sets the ATTRIBUTTES value for this DocumentDataReturn.
     * 
     * @param ATTRIBUTTES
     */
    public void setATTRIBUTTES(br.andrew.sap.document.Attributtes[] ATTRIBUTTES) {
        this.ATTRIBUTTES = ATTRIBUTTES;
    }


    /**
     * Gets the ELECTRONICFILE value for this DocumentDataReturn.
     * 
     * @return ELECTRONICFILE
     */
    public br.andrew.sap.document.ElectronicFile[] getELECTRONICFILE() {
        return ELECTRONICFILE;
    }


    /**
     * Sets the ELECTRONICFILE value for this DocumentDataReturn.
     * 
     * @param ELECTRONICFILE
     */
    public void setELECTRONICFILE(br.andrew.sap.document.ElectronicFile[] ELECTRONICFILE) {
        this.ELECTRONICFILE = ELECTRONICFILE;
    }


    /**
     * Gets the ERROR value for this DocumentDataReturn.
     * 
     * @return ERROR
     */
    public java.lang.String getERROR() {
        return ERROR;
    }


    /**
     * Sets the ERROR value for this DocumentDataReturn.
     * 
     * @param ERROR
     */
    public void setERROR(java.lang.String ERROR) {
        this.ERROR = ERROR;
    }

    private java.lang.Object __equalsCalc = null;
    public synchronized boolean equals(java.lang.Object obj) {
        if (!(obj instanceof DocumentDataReturn)) return false;
        DocumentDataReturn other = (DocumentDataReturn) obj;
        if (obj == null) return false;
        if (this == obj) return true;
        if (__equalsCalc != null) {
            return (__equalsCalc == obj);
        }
        __equalsCalc = obj;
        boolean _equals;
        _equals = true && 
            ((this.IDDOCUMENT==null && other.getIDDOCUMENT()==null) || 
             (this.IDDOCUMENT!=null &&
              this.IDDOCUMENT.equals(other.getIDDOCUMENT()))) &&
            ((this.NMTITLE==null && other.getNMTITLE()==null) || 
             (this.NMTITLE!=null &&
              this.NMTITLE.equals(other.getNMTITLE()))) &&
            ((this.IDCATEGORY==null && other.getIDCATEGORY()==null) || 
             (this.IDCATEGORY!=null &&
              this.IDCATEGORY.equals(other.getIDCATEGORY()))) &&
            ((this.NMCATEGORY==null && other.getNMCATEGORY()==null) || 
             (this.NMCATEGORY!=null &&
              this.NMCATEGORY.equals(other.getNMCATEGORY()))) &&
            ((this.STATUS==null && other.getSTATUS()==null) || 
             (this.STATUS!=null &&
              this.STATUS.equals(other.getSTATUS()))) &&
            this.NRHITS == other.getNRHITS() &&
            ((this.NMAUTHOR==null && other.getNMAUTHOR()==null) || 
             (this.NMAUTHOR!=null &&
              this.NMAUTHOR.equals(other.getNMAUTHOR()))) &&
            ((this.IDREVISION==null && other.getIDREVISION()==null) || 
             (this.IDREVISION!=null &&
              this.IDREVISION.equals(other.getIDREVISION()))) &&
            ((this.DTDOCUMENT==null && other.getDTDOCUMENT()==null) || 
             (this.DTDOCUMENT!=null &&
              this.DTDOCUMENT.equals(other.getDTDOCUMENT()))) &&
            this.QTVALIDITY == other.getQTVALIDITY() &&
            ((this.FGMEASVALID==null && other.getFGMEASVALID()==null) || 
             (this.FGMEASVALID!=null &&
              this.FGMEASVALID.equals(other.getFGMEASVALID()))) &&
            ((this.REVALIDATION==null && other.getREVALIDATION()==null) || 
             (this.REVALIDATION!=null &&
              this.REVALIDATION.equals(other.getREVALIDATION()))) &&
            ((this.DTVALIDITY==null && other.getDTVALIDITY()==null) || 
             (this.DTVALIDITY!=null &&
              this.DTVALIDITY.equals(other.getDTVALIDITY()))) &&
            ((this.RESUME==null && other.getRESUME()==null) || 
             (this.RESUME!=null &&
              this.RESUME.equals(other.getRESUME()))) &&
            ((this.CANCELREASON==null && other.getCANCELREASON()==null) || 
             (this.CANCELREASON!=null &&
              this.CANCELREASON.equals(other.getCANCELREASON()))) &&
            ((this.URL==null && other.getURL()==null) || 
             (this.URL!=null &&
              this.URL.equals(other.getURL()))) &&
            ((this.LINKSHARE==null && other.getLINKSHARE()==null) || 
             (this.LINKSHARE!=null &&
              this.LINKSHARE.equals(other.getLINKSHARE()))) &&
            ((this.ATTRIBUTTES==null && other.getATTRIBUTTES()==null) || 
             (this.ATTRIBUTTES!=null &&
              java.util.Arrays.equals(this.ATTRIBUTTES, other.getATTRIBUTTES()))) &&
            ((this.ELECTRONICFILE==null && other.getELECTRONICFILE()==null) || 
             (this.ELECTRONICFILE!=null &&
              java.util.Arrays.equals(this.ELECTRONICFILE, other.getELECTRONICFILE()))) &&
            ((this.ERROR==null && other.getERROR()==null) || 
             (this.ERROR!=null &&
              this.ERROR.equals(other.getERROR())));
        __equalsCalc = null;
        return _equals;
    }

    private boolean __hashCodeCalc = false;
    public synchronized int hashCode() {
        if (__hashCodeCalc) {
            return 0;
        }
        __hashCodeCalc = true;
        int _hashCode = 1;
        if (getIDDOCUMENT() != null) {
            _hashCode += getIDDOCUMENT().hashCode();
        }
        if (getNMTITLE() != null) {
            _hashCode += getNMTITLE().hashCode();
        }
        if (getIDCATEGORY() != null) {
            _hashCode += getIDCATEGORY().hashCode();
        }
        if (getNMCATEGORY() != null) {
            _hashCode += getNMCATEGORY().hashCode();
        }
        if (getSTATUS() != null) {
            _hashCode += getSTATUS().hashCode();
        }
        _hashCode += getNRHITS();
        if (getNMAUTHOR() != null) {
            _hashCode += getNMAUTHOR().hashCode();
        }
        if (getIDREVISION() != null) {
            _hashCode += getIDREVISION().hashCode();
        }
        if (getDTDOCUMENT() != null) {
            _hashCode += getDTDOCUMENT().hashCode();
        }
        _hashCode += getQTVALIDITY();
        if (getFGMEASVALID() != null) {
            _hashCode += getFGMEASVALID().hashCode();
        }
        if (getREVALIDATION() != null) {
            _hashCode += getREVALIDATION().hashCode();
        }
        if (getDTVALIDITY() != null) {
            _hashCode += getDTVALIDITY().hashCode();
        }
        if (getRESUME() != null) {
            _hashCode += getRESUME().hashCode();
        }
        if (getCANCELREASON() != null) {
            _hashCode += getCANCELREASON().hashCode();
        }
        if (getURL() != null) {
            _hashCode += getURL().hashCode();
        }
        if (getLINKSHARE() != null) {
            _hashCode += getLINKSHARE().hashCode();
        }
        if (getATTRIBUTTES() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getATTRIBUTTES());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getATTRIBUTTES(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getELECTRONICFILE() != null) {
            for (int i=0;
                 i<java.lang.reflect.Array.getLength(getELECTRONICFILE());
                 i++) {
                java.lang.Object obj = java.lang.reflect.Array.get(getELECTRONICFILE(), i);
                if (obj != null &&
                    !obj.getClass().isArray()) {
                    _hashCode += obj.hashCode();
                }
            }
        }
        if (getERROR() != null) {
            _hashCode += getERROR().hashCode();
        }
        __hashCodeCalc = false;
        return _hashCode;
    }

    // Type metadata
    private static org.apache.axis.description.TypeDesc typeDesc =
        new org.apache.axis.description.TypeDesc(DocumentDataReturn.class, true);

    static {
        typeDesc.setXmlType(new javax.xml.namespace.QName("urn:document", "documentDataReturn"));
        org.apache.axis.description.ElementDesc elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDDOCUMENT");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDDOCUMENT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMTITLE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMTITLE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDCATEGORY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMCATEGORY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMCATEGORY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("STATUS");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "STATUS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NRHITS");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NRHITS"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("NMAUTHOR");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "NMAUTHOR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("IDREVISION");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "IDREVISION"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("DTDOCUMENT");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DTDOCUMENT"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("QTVALIDITY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "QTVALIDITY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "int"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("FGMEASVALID");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "FGMEASVALID"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("REVALIDATION");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "REVALIDATION"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("DTVALIDITY");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "DTVALIDITY"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("RESUME");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "RESUME"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("CANCELREASON");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "CANCELREASON"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("URL");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "URL"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("LINKSHARE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "LINKSHARE"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ATTRIBUTTES");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ATTRIBUTTES"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "attributtes"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ELECTRONICFILE");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ELECTRONICFILE"));
        elemField.setXmlType(new javax.xml.namespace.QName("urn:document", "electronicFile"));
        elemField.setNillable(false);
        elemField.setItemQName(new javax.xml.namespace.QName("urn:document", "item"));
        typeDesc.addFieldDesc(elemField);
        elemField = new org.apache.axis.description.ElementDesc();
        elemField.setFieldName("ERROR");
        elemField.setXmlName(new javax.xml.namespace.QName("urn:document", "ERROR"));
        elemField.setXmlType(new javax.xml.namespace.QName("http://www.w3.org/2001/XMLSchema", "string"));
        elemField.setNillable(false);
        typeDesc.addFieldDesc(elemField);
    }

    /**
     * Return type metadata object
     */
    public static org.apache.axis.description.TypeDesc getTypeDesc() {
        return typeDesc;
    }

    /**
     * Get Custom Serializer
     */
    public static org.apache.axis.encoding.Serializer getSerializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanSerializer(
            _javaType, _xmlType, typeDesc);
    }

    /**
     * Get Custom Deserializer
     */
    public static org.apache.axis.encoding.Deserializer getDeserializer(
           java.lang.String mechType, 
           java.lang.Class _javaType,  
           javax.xml.namespace.QName _xmlType) {
        return 
          new  org.apache.axis.encoding.ser.BeanDeserializer(
            _javaType, _xmlType, typeDesc);
    }

}
