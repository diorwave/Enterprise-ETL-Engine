# ZXUN CG ASN.1 CDR Format Description (PS)

## Legal Information

By accepting this certain document of ZTE CORPORATION you agree to the following terms. If you do not agree to the following terms, please notice that you are not allowed to use this document.

Copyright © 2019 ZTE CORPORATION. Any rights not expressly granted herein are reserved.

This document contains proprietary information of ZTE CORPORATION. Any reproduction, transfer, distribution, use or disclosure of this document or any portion of this document, in any form by any means, without the prior written consent of ZTE CORPORATION is prohibited.

ZTE is registered trademarks of ZTE CORPORATION. ZTE's company name, logo and product names referenced herein are either trademarks or registered trademarks of ZTE CORPORATION. Other product and company names mentioned herein may be trademarks or trade names of their respective owners. Without the prior written consent of ZTE CORPORATION or the third party owner thereof, anyone's access to this document should not be construed as granting, by implication, estoppel or otherwise, any license or right to use any marks appearing in the document.

The design of this product complies with requirements of environmental protection and personal security. This product shall be stored, used or discarded in accordance with product manual, relevant contract or laws and regulations in relevant country (countries).

This document is provided "as is" and "as available". Information contained in this document is subject to continuous update without further notice due to improvement and update of ZTE CORPORATION's products and technologies.

**ZTE CORPORATION**
- Address: NO. 55 Hi-tech Road South, ShenZhen, P.R.China 518057
- Website: http://support.zte.com.cn
- Email: 800@zte.com.cn

## Revision History

| Old Version | New Version | Date | Content | Remarks |
|------------|-------------|------|---------|---------|
| - | V6.0.0 | 2009-11-3 | Created | ZXUN-eCG V4.10.10.P2 |
| V6.0.0 | V6.1.0 | 2010-10-23 | Add DiameterSessionID to GCDR | ZXUN-eCG V4.10.10.P3 |
| V6.1.0 | V6.2.0 | 2010-11-18 | 1. Add localSequenceNumber,servingNodeAddress,userLocationInformation to ChangeOfServiceCondition<br>2. Add asn.1 description to ServiceConditionChange<br>3. Add DiameterSessionID to PGWCDR<br>4. Add servedPDPPDNAddressExt to GCDR,SGWCDR and PGWCDR<br>5. Add W-CDR | ZXUN-eCG V4.10.10.P4 |
| V6.2.0 | V6.2.1 | 2010-12-15 | Correct the spelling error | ZXUN-eCG V4.10.10.P4 |
| V6.2.1 | V6.2.1 | 2011-1-18 | Add Ipv4/Ipv6 asn.1 description to PDPType field<br>Modify the value of "Best effort" to 31 in QoSMeanThroughput | ZXUN-eCG V4.10.10.P4 |
| V6.2.1 | V6.2.1 | 2011-6-30 | Add RAI description to UmtsUserLocationInformation field | ZXUN-eCG V4.10.10.P7 |
| V6.2.1 | V6.3.0 | 2011-8-11 | 1. Correct the definition of serviceConditionChange<br>2. Correct the definition of url and diametersessionID to IA5String<br>3. Modify tag values<br>4. Modify tag values for various fields<br>5. Delete tag 0x30 for specific fields<br>6. Modify tag values for servingNodeType<br>7. Modify tag values for rAIdentity<br>8. Add iMSIunauthenticatedFlag to GCDR,SGWCDR and PGWCDR | ZXUN-CG V4.11.20.Bx |
| V6.3.0 | V6.4.0 | 2012-1-5 | 1. Add servedPDPPDNAddressExt to SCDR<br>2. Add pSFurnishChargingInformation to GCDR | ZXUN-CG V4.11.20.P1 |
| V6.4.0 | V6.4.1 | 2012-3-6 | 1. Add serviceChargeType to ContentChargeInformation and ChangeOfServiceCondition<br>2. Add userLocationInformation to ContentChargeInformation<br>3. Add HSGW-CDR | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.1 | 2012-6-15 | Correct the size of UmtsQosInformation | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.1 | 2012-6-20 | 1. Correct the size of serviceKey to octet string(size(1..4))<br>2. Add subscribedDelayClass to QosDelay<br>3. Correct the definition for zero in QoS fields | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.2 | 2012-7-2 | Add cellPLMNID field to MCDR ,SMO and SMT | ZXUN-CG V4.11.20.P3 |
| V6.4.1 | V6.4.2 | 2012-7-12 | Modify sgsnPLMNIdentifier of S-CDR to cellPLMNID | ZXUN-CG V4.11.20.P3 |
| V6.4.2 | V6.4.2 | 2012-8-2 | Correct the description of HSGW-CDR.ServedHardwareID | ZXUN-CG V4.11.20.P3 |
| V6.4.2 | V6.5.0 | 2012-8-28 | 1. Add MBMS-GW-CDR<br>2. Add various EPC fields<br>3. Add userCSGInformation to SGWCDR and PGWCDR | ZXUN-CG V4.12.10.Bx |
| V6.5.0 | V6.5.0 | 2012-9-21 | Modify pdpType of all record types | ZXUN-CG V4.12.10.Bx |
| V6.5.0 | V6.5.1 | 2012-12-13 | Add pSFurnishChargingInformation in PGW-CDR | ZXUN-CG V4.12.10.P1 |
| V6.5.1 | V6.5.1 | 2013-1-17 | Various corrections and improvements | ZXUN-CG V4.12.10.P1 |
| V6.5.1 | V6.5.1 | 2013-1-31 | Correct servedIMSI to OPTIONAL in all CDR | ZXUN-CG V4.12.10.P1 |
| V6.5.2 | V6.5.1 | 2013-4-16 | Modify the ASN.1 definition of LCS CDR according to 3GPP R9 | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.2 | 2013-5-27 | Add the rATType definitions(101~105) | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.2 | 2013-8-13 | Correct the definition of LCSClientExternalID | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.3 | 2013-8-13 | Add the ServiceChargeType definitions(3~6) | ZXUN-CG V4.13.10.Bx |
| V6.5.3 | V6.5.3 | 2013-8-29 | Add the bit definition of MsTimeZone | ZXUN-CG V4.13.10.Bx |
| V6.5.3 | V6.6.0 | 2013-9-17 | 1. Redefined the hSGWRecord according to China TeleCom LTE specification<br>2. Correct the asn.1 definition to match the standard | ZXUN-CG V4.13.10.Bx |
| V6.6.0 | V6.6.0 | 2013-12-10 | Add the enumerated value: ePCF (100) in ServingNodeType | ZXUN-CG V4.13.10.Bx |
| V6.6.0 | V6.6.0 | 2014-01-15 | Correct the definition of WLAN.servingProxyAddress as GSNAddress | ZXUN-CG V4.13.10.P1 |
| V6.6.0 | V6.6.1 | 2014-2-14 | servedPDPPDNAddress add prefixlength in SGW-CDR and PGW-CDR | ZXUN-CG V4.13.10.P1 |
| V6.6.1 | V6.6.2 | 2014-4-11 | Add threeGPP2UserLocationInformation to PGW-CDR and ChangeOfServiceCondition | ZXUN-CG V4.13.10.P2 |
| V6.6.2 | V6.6.3 | 2014-6-19 | Add chargingRuleBaseName and aFRecordInformation to listOfServiceData of PGW-CDR | ZXUN-CG V4.13.10.P3 |
| V6.6.3 | V6.6.3 | 2014-7-31 | 1. Delete "Each CDR length can not exceed max.2048 bytes." at chapter 1.2.2<br>2. Correct block size description | V4.13.10.P3 |
| V6.6.3 | V6.6.4 | 2014-9-24 | Correct the tag value definition for qos of listOfTrafficVolumes field in GWMBMSRecord<br>Add pcrfProvideNewQoS to CauseForRecClosing | V4.13.10.P4 |
| V6.6.4 | V6.7.0 | 2014-12-11 | Correct the category to OPTIONAL for some fields which are not generated in CG | V4.14.10.P1 |
| V6.7.0 | V6.7.1 | 2015-4-8 | Add ePDG-CDR<br>Add tWANUserLocationInformation to PGW-CDR | V4.14.10.P2 |
| V6.7.1 | V6.7.2 | 2015-7-3 | Add listOfTrafficVolumes to PGW-CDR | V4.14.10.P3 |
| V6.7.2 | V6.7.3 | 2015-8-3 | 1. Add sponsorIdentity,applicationServiceProviderIdentity to listOfServiceData in PGW-CDR<br>2. Add ePCQoSInformation to G-CDR and PGW-CDR<br>3. Add listOfTrafficVolumes.qosNegotiated to PGW-CDR<br>4. Add aPNAggregateMaxBitrateUL and aPNAggregateMaxBitrateDL to EPCQoSInformation | V4.14.10.P4 |
| V6.7.3 | V6.7.4 | 2016-2-23 | Add dynamicAddressFlagExt in SGW-CDR and PGW-CDR | V4.14.10.P7 |
| V6.7.4 | V6.7.5 | 2016-8-8 | Add uWANUserLocationInformation in EPDG-CDR and PGW-CDR<br>Add eUTRAN-NB-IoT to RATType | V4.14.10.P8 |
| V6.7.5 | V6.7.6 | 2016-9-14 | Add url to G-CDR::recordExtensions.contentCharge<br>Add url to PGW-CDR::listOfServiceData | ZXUN-CG V5.16.10 |
| V6.7.6 | V7.0.0 | 2016-11-28 | Add cPCIoTEPSOptimisationIndicator in SGW-CDR | V5.16.10.P1 |
| V7.0.0 | V7.1.0 | 2017-4-10 | 1. Add various fields to SGW-CDR and PGW-CDR<br>2. Add new cause values<br>3. Add various control parameters | V5.17.10.B2 |
| V7.1.0 | V7.1.0 | 2017-7-11 | Add sCSASAddress to PGW-CDR | V5.17.10.P1 |
| V7.1.0 | V7.1.0 | 2018-2-12 | Refine the description of the "service condition change extensions" field | V5.17.10.P1 |
| V7.2.0 | V7.2.0 | 2018-2-26 | Add extended bandwidth fields to EPCQoSInformation | V5.18.10.B4 |
| V7.2.0 | 7.2.1 | 2018-5-17 | Add listOfRANSecondaryRATUsageReports to SGW-CDR and PGW-CDR | V5.18.10.P2.B4 |

## Table of Contents

1. **GENERAL INFORMATION**
   1.1 Abbreviation
   1.2 Structure of the CDR File
      1.2.1 Field
      1.2.2 Charging Data Record
      1.2.3 Block
      1.2.4 CDR File
   1.3 CDR Encoding
      1.3.1 ASN.1 (Basic Encoding Rules) description
      1.3.2 Encoding of the Tag
      1.3.3 Encoding of the length
      1.3.4 Encoding of the content
      1.3.5 ASN.1 BER Encoding of Integers
      1.3.6 Structure Diagrams of Charging Data Record

2. **Description of Various CDRs**
   2.1 General
   2.2 Field category type description
   2.3 Record Types
      2.3.1 S-CDR(SGSNPDPRecord)
      2.3.2 G-CDR(GGSNPDPRecord)
      2.3.3 M-CDR(SGSNMMRecord)
      2.3.4 S-SMO-CDR(SGSNSMORecord)
      2.3.5 S-SMT-CDR(SGSNSMTRecord)
      2.3.6 LCS-MO-CDR(SGSNMOLCSREcord)
      2.3.7 LCS-MT-CDR(SGSNMTLCSRecord)
      2.3.8 LCS-NI-CDR(SGSNNILCSRecord)
      2.3.9 S-MB-CDR(SGSNMBMSRecord)
      2.3.10 G-MB-CDR(GGSNMBMSRecord)
      2.3.11 SGW-CDR(SGWRecord)
      2.3.12 PGW-CDR(PGWRecord)
      2.3.13 W-CDR(WLANRecord)
      2.3.14 HSGW-CDR(HSGWRecord)
      2.3.15 MBMS-GW-CDR(GWMBMSRecord)
      2.3.16 ePDG-CDR(EPDGRecord)
   2.4 Description of Fields

3. **ASN.1 Definitions for CDR**
   3.1 CDR Structure
   3.2 CDR Fields Structure
   3.3 TAG Values

4. **Annex**
   4.1 Description of Version Number


---

## 1. GENERAL INFORMATION

### 1.1 Abbreviation

| Abbr. | Full Name |
|-------|-----------|
| APN | Access Point Name |
| CDR | Charging Data Record |
| C-ID | Charging ID |
| CG | Charging Gateway |
| CGF | Charging Gateway Functionality |
| GTP | GPRS Tunnel Protocol |
| GGSN | Gateway GPRS Support Node |
| G-CDR | Gateway GPRS Support Node-Call Detail Record |
| IMSI | International Mobile Subscriber Identifier |
| IP | Internet Protocol |
| LAC | Location Area Identifier |
| MBMS | Multimedia Broadcast/Multicast Service |
| MS | Mobile Station |
| M-CDR | Mobile Management-Call Detail Record |
| PDN | Packet Data Network |
| PDP | Packet Data Protocol |
| PLMN | Public Land Mobile Network |
| PPP | Point to Point Protocol |
| RAC | Routing Area Code |
| RAI | Routing Area Identifier |
| SGSN | Service GPRS Support Node |
| S-CDR | Serving GPRS Support Node-Call Detail Record |
| S-SMO-CDR | SGSN delivered Short Message Mobile Originated-Call Detail Record |
| S-SMT-CDR | SGSN delivered Short Message Mobile Terminated-Call Detail Record |
| PCN | Packet switched Core network Node (SGSN, S–GW, P–GW) |
| S-GW | Serving GateWay |
| P-GW | PDN GateWay |
| SGW-CDR | Serving GateWay-Call Detail Record |
| PGW-CDR | PDN GateWay-Call Detail Record |

### 1.2 Structure of the CDR File

#### 1.2.1 Field
Basic data unit, which is the basic element forming the ticket record. Each field has its own tag and length (Len). The field can be divided into fixed length field and non-fixed length field.

#### 1.2.2 Charging Data Record
Record the billing information related to a chargeable event. Each CDR contains multiple fields.

#### 1.2.3 Block
The block contains one or more (n) CDRs with variable lengths (L). The block has a fixed size which can be configured to 2048 or 4096 or 8192 bytes. It is filled with CDRs until there is no more room to add the next record. The rest of the fixed size block will then be filled with fillers(H'FF).

**Figure 1: Structure Diagrams of Block**
```
+-------+-------+-----+-------+---------+
| CDR1  | CDR2  | ... | CDRn  | fillers |
|  L1   |  L2   |     |  Ln   |         |
+-------+-------+-----+-------+---------+
        Block size=2048/4096/8192 bytes
```

#### 1.2.4 CDR File
The CDR file consists of one or multiple "Blocks" when organized by block. GSN/CGF will generate a CDR file at the specified time or the specified length according to the system configuration.

**Figure 2: Structure Diagrams of CDR File organized by block**
```
+--------+--------+--------+-----+--------+
| Block1 | Block2 | Block3 | ... | Block n|
+--------+--------+--------+-----+--------+
            CDR File size
```

The CDR file consists of one or multiple "CDRs" when organized by CDR. GSN/CGF will generate a CDR file at the specified time or the specified length according to the system configuration.

**Figure 3: Structure Diagrams of CDR File organized by CDR**
```
+------+------+------+-----+------+
| CDR1 | CDR2 | CDR3 | ... | CDRn |
+------+------+------+-----+------+
         CDR File size
```


### 1.3 CDR Encoding

#### 1.3.1 ASN.1 (Basic Encoding Rules) description

The CDRs generated at GSN/CGF is specified using the Abstract Syntax Notation One (ASN.1). ASN.1 is a language that defines the way data is sent across dissimilar communication systems. ASN.1 ensures that the data received is the same as the data transmitted by providing a common syntax for specifying application layer protocols. ASN.1 is an ISO/ITU-T standard based on the OSI model and is defined in "ASN.1 encoding rules: Specification of Basic Notation, ITU-T Recommendation X.680".

The GSN/CGF uses ASN.1 Basic Encoding Rules (BER) to encode the CDRs. BER is a set of standard rules, defined in "ASN.1 encoding rules: Specification of Basic Encoding Rules (BER), Canonical Encoding Rules (CER) and Distinguished Encoding Rules (DER) ITU-T Recommendation X.690", for encoding data types specified in ASN.1. A field containing a value of a certain data type is encoded into the following parts:
- **Identifier**: identifies the data type and consists of an ASN.1 tag and data structure information
- **Length**: the length of the content part in number of octets
- **Contents**: the value encoded according to data type specific rules

The ASN.1 tags assigned to CDR fields are specified in chapter 3.3.

#### 1.3.2 Encoding of the Tag

The identifier octets encode the ASN.1 tag of the data value. Two possibilities exist:

**A. Single octet encoding for tag numbers from 0 to 30 (inclusive)**

```
Octet 1:  [CLASS | P/C | NUMBER of TAG]
           8 7     6     5 4 3 2 1
```

- **Bits 8-7**: Class identifier
  - 00: Universal
  - 01: Application  
  - 10: Context-specific
  - 11: Private

- **Bit 6**: 
  - 0: Primitive
  - 1: Constructed

- **Bits 5-1**: binary integer with bit 5 as msb

**B. Use of a leading octet for tag numbers bigger than or equal to 31**

The leading octet is encoded as follows:
```
Octet 1:  [CLASS | P/C | 11111]
           8 7     6     5-1

Subsequent octets:
- Bit 8: set to 1 in all non-last subsequent octets
- Bits 7-1: Bits 7-1 of all subsequent octets encoded as a binary integer equal to the tag number
```

#### 1.3.3 Encoding of the length

The length octets encode the length of the following content of the data item. Three possibilities exist in ASN.1: short, long and indefinite. The indefinite variant is not used in this CDR format.

**A. Short length encoding for length from 0 to 127 (inclusive)**
```
Octet 1: [0|LLLLLLL]
          8  7-1
```
LLLLLLL represents the length of the content

**B. Long length encoding for length > 127**
```
Octet 1:   [1|n (where n < 127)]
Octet 2-n+1: Length value
```


#### 1.3.4 Encoding of the content

The numbering of bits within one octet and the encoding of a binary value in an octet structure:
```
Octet 1:  [most significant byte]
Octet 2:  [...]  
Octet n:  [least significant byte]
           8 7 6 5 4 3 2 1
```
- bit 8 of octet 1 is the most significant bit (msb)
- bit 1 of octet n is the least significant bit (lsb)

#### 1.3.5 ASN.1 BER Encoding of Integers

ASN.1 defines the integer type as a simple type with distinguished values, which are the positive and negative numbers, including zero. The content part for a field of type integer is BER encoded in one or more octets. The content octets contain the two's complement binary number equal to the integer value, and consisting of bits 8 to 1 of the first octet, followed by bit 8 to 1 of each octet in turn up to and including the last content octet. 

The two's complement form implies that the most significant bit of the content octets indicates the sign of the value (0 for positive numbers and 1 for negative numbers). This means that the highest positive number a sequence of X octets can represent is: 2^(8X-1)-1.

**Example 1:**
The highest positive number represented by four octets (32 bits) is: 2^31-1.

**Example 2:**
To represent the highest value for a 32 bits positive integer (2^32-1), five content octets are needed.

#### 1.3.6 Structure Diagrams of Charging Data Record

**Figure 4: Structure Diagrams of Charging Data Record**

```
+---------------------------+
| TAG = Charging Data       |
| Record Tag                |
+---------------------------+
| Length of Charging Data   |
| Record                    |
+---------------------------+
| TAG = Field Tag 1         |
+---------------------------+
| Length of Field 1         |
+---------------------------+
| Value of Field 1          |
+---------------------------+
| ...                       |
+---------------------------+
| TAG = Field Tag m         |
+---------------------------+
| Length of Field m         |
+---------------------------+
| H'30                      |
| Value of Sequence 1       |
| H'30                      |
| Value of Sequence 2       |
| ...                       |
+---------------------------+
| TAG = Field Tag n         |
+---------------------------+
| Length of Field n         |
+---------------------------+
| Value of Field n          |
| (With Sequence)           |
+---------------------------+
```


---

## 2. Description of Various CDRs

### 2.1 General

The SGSN shall collect the following charging information:
- **Usage of the radio interface**: The charging information shall provide respectively the amount of data in MO and MT directions and reflect PDP protocol type and QoS information
- **Usage of PDP address**: The charging information shall provide the period of usage of PDP addresses by the MS
- **Usage of the general GPRS resources**: The charging information shall provide MS's usage of other GPRS-related resources (e.g. mobility management overhead)
- **Location of MS**: The charging information shall provide HPLMN, VPLMN, and accurate location information (e.g. RAI, CI)

The GGSN shall collect the following charging information:
- **Destination and source**: The charging information shall provide destination and source addresses for the PDP context
- **Usage of the external data networks**: The charging information shall describe the amount of data sent and received to and from the external data network
- **Usage of PDP address**: The charging information shall provide the period of usage of PDP addresses by the MS
- **Location of MS**: The charging information shall provide HPLMN, VPLMN, and accurate location information. For GGSN, the accurate location information of MS is SGSN address

The S-GW shall collect the following charging information:
- **Usage of the radio interface**: The charging information shall provide respectively the amount of data in MO and MT directions and reflect PDP protocol type and QoS information
- The charging information shall provide the duration of the IP-CAN bearer with date and time information
- **Usage of the general Packet-Switched domain resources**: The charging information shall provide MS's usage of other Packet-Switched domain-related resources (e.g. mobility management overhead)
- **Location of MS**: The charging information shall provide HPLMN, VPLMN, and accurate location information (e.g. RAI,LAC,CI)

The P-GW shall collect the following charging information:
- **Destination and source**: The charging information shall provide destination and source addresses for the IP CAN bearer
- Data volumes on both the uplink and downlink direction shall be counted separately. The data volumes shall reflect the data as delivered to and from the user
- The charging information shall provide the duration of the IP-CAN bearer with date and time information
- **Location of MS**: The charging information shall provide HPLMN, VPLMN, and accurate location information. For GGSN, the accurate location information of MS is SGSN address
- The P-GW may be capable of identifying data volumes, elapsed time or events for individual service data flows (flow based bearer charging). One PCC rule identifies one service data flow

### 2.2 Field category type description

- **M**: This field is Mandatory and shall always be present in the CDR
- **C**: This field shall be present in the CDR only when certain Conditions are met. These Conditions are specified as part of the field definition
- **O_M**: This is a field that, if provisioned by the operator to be present, shall always be included in the CDRs. In other words, an O_M parameter that is provisioned to be present is a mandatory parameter
- **O_C**: This is a field that, if provisioned by the operator to be present, shall be included in the CDRs when the required conditions are met. In other words, an O_C parameter that is configured to be present is a conditional parameter


### 2.3 Record Types

#### 2.3.1 S-CDR(SGSNPDPRecord)

S-CDR is used to collect charging data of MS-related IP CAN bearer in the SGSN. The trigger conditions of generating S-CDR in SGSN include: termination of the IP CAN bearer, data volume limit, time limit and changing of charging conditions up to the maximum number.

**Table 2: SGSN IP CAN bearer Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN IP CAN bearer record |
| Network Initiated PDP Context | O_C | A flag that is present if this is a network initiated IP CAN bearer |
| Served IMSI | O_C | IMSI of the served party |
| Served IMEI | O_C | The IMEI of the ME, if available |
| List of SGSN Address | O_C | The record is the current SGSN address before consolidation, and IP CAN bearer-involved IP address list of SGSN after consolidation |
| MS Network Capability | O_C | The mobile station Network Capability |
| Routing Area Code (RAC) | O_C | RAC at the time of "Record Opening Time" |
| Location Area Code (LAC) | O_C | LAC at the time of "Record Opening Time" |
| Cell Identifier | O_C | Cell identity for GSM or Service Area Code (SAC) for UMTS at the time of "Record Opening Time" |
| Charging ID | O_C | IP CAN bearer identifier used to identify this IP CAN bearer in different records created by PCNs |
| GGSN Address Used | O_C | The control plane IP address of the P-GW currently used. The P-GW address is always the same for an activated IP CAN bearer |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP Type | O_C | PDP type, i.e. IP, PPP, IHOSS:OSP |
| Served PDP Address | O_C | PDP address of the served IMSI, i.e. Ipv4 or Ipv6. This parameter shall be present except when both the PDP type is PPP and dynamic PDP address assignment is used |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this IP CAN bearer, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data volumes are also listed |
| Record Opening Time | O_C | Time stamp when IP CAN bearer is activated in this SGSN or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the SGSN |
| SGSN Change | O_C | Present if this is first record after SGSN change |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| List of Record Sequence Number | O_C | Partial record sequence number in this SGSN. Only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Access Point Name Operator Identifier | O_C | The Operator Identifier part of the APN |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP CAN bearer |
| System Type | O_C | This field indicates the Radio Access Technology (RAT) type, e.g. UTRAN or GERAN, currently used by the Mobile Station as defined in TS 29.060 [204] |
| CAMEL Information | O_C | Set of CAMEL information related to IP CAN bearer. For more information see Description of Record Fields. This field is present if CAMEL service is activated |
| RNC Unsent Downlink Volume | O_C | The downlink data volume, which the RNC has not sent to MS. This field is present when the RNC has provided unsent downlink volume count at RAB release |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected |
| Dynamic Address Flag | O_C | Indicates whether served PDP address is dynamic, which is allocated during IP CAN bearer activation. This field is missing if address is static |
| Consolidation Result | O_C | The consolidation result of partial record generated from one PDP context, only occurs in consolidated record |
| List of Local Record Sequence Number | O_C | Local sequence number of the original part of record and correspondent SGSN address form a list, which occurs only in consolidated record |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time |
| Served PDP/PDN Address Extension | O_C | This field contains the Ipv4 address for the PDN connection when dual-stack Ipv4 Ipv6 is used |

#### 2.3.2 G-CDR(GGSNPDPRecord)

G-CDR is used to collect charging data of MS-related PDP context in the GGSN. The trigger conditions of generating G-CDR in GGSN include: termination of the PDP context, data volume limit, time limit and changing of charging conditions up to the maximum number.

**Table 3: GGSN PDP Context Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | GGSN PDP context record |
| Network initiated PDP context | O_C | A flag indicating whether this is a network initiated PDP context |
| Served IMSI | O_C | IMSI of the served party (non-anonymous connection) |
| GGSN Address used | O_C | The control plane IP address of the GGSN used |
| Charging ID | O_C | PDP context identifier used to identify this PDP context in different records created by GSNs |
| SGSN Address | O_C | List of SGSN addresses used during this record |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP Type | O_C | PDP type, i.e. IP, PPP, IHOSS:OSP |
| Served PDP Address | O_C | PDP address, i.e. Ipv4 or Ipv6. This parameter shall be present except when both the PDP type is PPP and dynamic PDP address assignment is used |
| Dynamic Address Flag | O_C | Indicates whether served PDP address is dynamic, which is allocated during PDP context activation. This field is missing if address is static |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this PDP context, each change is time stamped. Charging conditions are used to categorise traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are listed |
| Record Opening Time | O_C | Time stamp when PDP context is activated in this GGSN or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the CDR |
| Cause for Record Closing | O_C | Reason for closure of the record |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| List of Record Sequence Number | O_C | For those records that are partial record S/Ns generated in GGSN before consolidation, and the S/N list of the original partial record after consolidation, the list includes GGSN addresses |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | Supplementary field (used for content charging) |
| Local Sequence Number | M | This node creates S/N of all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| Charging Characteristics | O_C | The Charging Characteristics applied to the PDP context |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected |
| SGSN PLMN Identifier | O_C | SGSN PLMN Identifier (MCC and MNC) used during this record |
| Consolidation Result | O_C | The consolidation result of partial record generated from one PDP context, only occurs in consolidated record |
| List of Local Record Sequence Number | O_C | Local sequence number of the original partial record and correspondent GGSN address form a list, which occurs only in consolidated record |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station. The field is present in the G-CDR if provided by SGSN |
| IMS Signalling Context | O_C | Included if the IM-CN Subsystem Signalling Flag is set, Which indicate PDP context is used for IMS signaling |
| External Charging Identifier | O_C | Holds a Charging Identifier and is present only if it is received from a non-GPRS, external network entity |
| User Location Information | O_C | This field contains the User Location Information of the MS, if provided by SGSN |
| Served IMEISV | O_C | IMEISV of the ME, if available |
| MS Time Zone | O_C | This field contains the MS Time Zone the MS is currently located as defined in TS 29.060, if provided by SGSN |
| URL | O_C | This field indicates the first visited URL of the subscriber |
| Roaming Indicator | O_C | This field indicates the roaming CDR |
| DiameterSessionID | O_C | This field is used to associate G-CDR with CDR generated by OCS, this field is also used to judge whether the flag of OCS is inuse |
| Served PDP/PDN Address Extension | O_C | This field contains the Ipv4 address for the PDN connection when dual-stack Ipv4 Ipv6 is used |
| IMSI Unauthenticated Flag | O_C | This field indicates the provided served IMSI is not authenticated (emergency bearer service situation) |
| PS Furnish Charging Information | O_C | This field contains charging information in case it is sent by OCS |
| EPC User Location Information | O_C | This field contains the User Location Information of the MS for EPC case if available as defined in 29.274 [210] |
| EPC Qos Information | O_C | Contains the QoS applied for IP CAN bearer for EPC case |

#### 2.3.3 M-CDR(SGSNMMRecord)

M-CDR is used to collect charging data of MS-related mobility management in the SGSN. M-CDR is collected when MS starts attachment, until mobile subscriber detach.

**Table 4: SGSN Mobile Station Mobility Management Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN mobility management record |
| Served IMSI | O_C | IMSI of the MS |
| Served IMEI | O_C | The IMEI of the ME, if available |
| SGSN Address | O_C | The IP address of the current SGSN |
| MS Network Capability | O_C | The mobile station network capability |
| Routing Area | O_C | Routing Area at the time of the Record Opening Time |
| Local Area Code | O_C | Location Area Code at the time of Record Opening Time |
| Cell Identifier | O_C | Cell identity or Service Area Code (SAC) at the time of "Record Opening Time" |
| Change of Location | O_C | A list of changes in Routing Area Code, each with a time stamp. This field is not required if partial records are generated when the location changes |
| Record Opening Time | O_C | Timestamp when MS is attached to this SGSN or record opening time on following partial record |
| Duration | O_C | Duration of this record |
| SGSN Change | O_C | Present if this is first record after SGSN change |
| Cause for Record Closing | O_C | The reason for the closure of the record in this SGSN |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number in this SGSN; only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| Charging Characteristics | O_C | The Charging Characteristics used by the SGSN |
| CAMEL Information | O_C | Set of CAMEL information related to Attach/Detach session. For more information see Description of Record Fields. This field is present if CAMEL service is activated |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time |

#### 2.3.4 S-SMO-CDR(SGSNSMORecord)

S-SMO-CDR shall be produced for each short message sent by an MS through the SGSN.

**Table 5: SGSN Mobile Originated SMS Record**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | The CDR is S-SMO-CDR |
| Served IMSI | O_C | The IMEI of the PS subscriber |
| Served IMEI | O_C | The IMEI of the PS subscriber |
| Served MSISDN | O_C | The MSISDN of the PS subscriber |
| MS Network Capability | O_C | The mobile station network capability |
| Service Centre | O_C | The address (E.164) of the SMS-service centre |
| Recording Entity | O_C | The E.164 number of the SGSN |
| Location Area Code | O_C | The Location Area Code from which the message originated |
| Routing Area Code | O_C | The Routing Area Code from which the message originated |
| Cell Identifier | O_C | The Cell or Service Area Code (SAC) from which the message originated |
| Message Reference | O_C | Identifier of the short message |
| Event Time Stamp | O_C | The time at which the message was received by the SGSN from the subscriber |
| SMS Result | O_C | The result of the attempted delivery if unsuccessful |
| Record Extensions | O_C | Supplementary field |
| Node ID | O_C | Code of SGSN |
| Local Sequence Number | M | This node creates S/N of all CDR types |
| Charging Characteristics | O_C | Flag of charging Characteristics in subscriber data |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface |
| Destination Number | O_C | The destination short message subscriber number |
| CAMEL Information | O_C | Set of CAMEL information related to SMS |
| Charging Characteristics selection mode | O_C | Indicates selection mode of charging characteristics |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time |

#### 2.3.5 S-SMT-CDR(SGSNSMTRecord)

S-SMT-CDR shall be produced for each short message received by an MS through the SGSN.

**Table 6: SGSN Mobile Received SMS Record**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | The CDR is S-SMT-CDR |
| Served IMSI | O_C | The IMEI of the PS subscriber |
| Served IMEI | O_C | The IMEI of the PS subscriber |
| Served MSISDN | O_C | The MSISDN of the PS subscriber |
| MS Network Capability | O_C | The mobile station network capability |
| Service Centre | O_C | The address (E.164) of the SMS-service centre |
| Recording Entity | O_C | The E.164 number of the SGSN |
| Location Area Code | O_C | The location area code from which the message is received |
| Routing Area Code | O_C | The routing area code from which the message is received |
| Cell Identifier | O_C | The Cell or Service Area Code (SAC) from which the message is received |
| Event Time Stamp | O_C | The time at which the message was received by the SGSN from the subscriber |
| SMS Result | O_C | The result of the attempted delivery if unsuccessful |
| Record Extensions | O_C | Supplementary field |
| Node ID | O_C | Code of SGSN |
| Local Sequence Number | M | This node creates S/N of all CDR types |
| Charging Characteristics | O_C | Flag of charging characteristics in subscriber data |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface |
| CAMEL Information | O_C | Set of CAMEL information related to SMS |
| Charging Characteristics selection mode | O_C | Indicates selection mode of charging characteristics |
| Origination Number | O_C | The origination short message subscriber number |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time |

#### 2.3.6 LCS-MO-CDR(SGSNMOLCSREcord)

An SGSN Mobile originated LCS record shall be produced for each mobile originated location request is performed via the SGSN

**Table 7: SGSN Mobile Originated LCS Record (SGSN-LCS-MO)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN Mobile Originated LCS |
| Recording Entity | O_C | The E.164 number of the SGSN |
| LCS Client Type | O_C | The type of the LCS client that invoked the LR, if available |
| LCS Client Identity | O_C | Further identification of the LCS client, if available |
| Served IMSI | O_C | The IMSI of the subscriber |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| SGSN Address | O_C | The IP address of the current SGSN |
| Location Method | O_C | The type of the location request |
| LCS QoS | O_C | QoS of the LR, if available |
| LCS Priority | O_C | Priority of the LR, if available |
| MLC Number | O_C | The E.164 address of the involved GMLC, if applicable |
| Event Time stamp | O_C | The time at which the Perform_Location_Request is sent by the SGSN |
| Measurement Duration | O_C | The duration of proceeding the location request |
| Location | O_C | The LAC and CI when the LR is received |
| Routing Area Code | O_C | The Routing Area Code from which the LCS originated |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful |
| Positioning Data | O_C | The positioning method used or attempted, if available |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened |
| Node ID | O_C | Name of the recording entity |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| Charging Characteristics | O_C | The Charging Characteristics flag set used by the SGSN |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN |

#### 2.3.7 LCS-MT-CDR(SGSNMTLCSRecord)

An SGSN Mobile terminated LCS record shall be produced for each mobile terminated location request is performed via the SGSN.

**Table 8: SGSN Mobile Terminated LCS Record (SGSN-LCS-MT)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN Mobile Terminated LCS |
| Recording Entity | O_C | The E.164 number of the SGSN |
| LCS Client Type | O_C | The type of the LCS client that invoked the LR |
| LCS Client Identity | O_C | Further identification of the LCS client |
| Served IMSI | O_C | The IMSI of the subscriber |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| SGSN Address | O_C | The IP address of the current SGSN |
| Location Type | O_C | The type of the estimated location |
| LCS QoS | O_C | QoS of the LR, if available |
| LCS Priority | O_C | Priority of the LR, if available |
| MLC Number | O_C | The E.164 address of the requesting GMLC |
| Event Time stamp | O_C | The time at which the Perform_Location_Request is sent by the SGSN |
| Measurement Duration | O_C | The duration of proceeding the location request |
| Notification To MS User | O_C | The privacy notification to MS user that was applicable when the LR was invoked, if available |
| Privacy Override | O_C | This parameter indicates the override MS privacy by the LCS client, if available |
| Location | O_C | The LAC and CI when the LR is received |
| Routing Area Code | O_C | The Routing Area Code to which the LCS terminated |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful |
| Positioning Data | O_C | The positioning method used or attempted, if available |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened |
| Node ID | O_C | Name of the recording entity |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| Charging Characteristics | O_C | The Charging Characteristics used by the SGSN. (always use the subscribed CC) |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected. (Only subscribed/home default/visited default) |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN |
 The Routing Area Code from which the LCS originated |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful |
| Positioning Data | O_C | The positioning method used or attempted, if available |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened |
| Node ID | O_C | Name of the recording entity |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| Charging Characteristics | O_C | The Charging Characteristics flag set used by the SGSN |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN |

#### 2.3.9 S-MB-CDR(SGSNMBMSRecord)

If the collection of CDR data is enabled then the SGSN data specified in the following table shall be available for each MBMS bearer context.

**Table 10: SGSN MBMS bearer context data (S-MB-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN MBMS bearer context record |
| GGSN Address used | O_C | The control plane IP address of the GGSN used |
| Charging ID | O_C | Bearer context identifier used to identify this MBMS bearer context in different records created by GSNs |
| List of RAs | O_C | List of routeing areas receiving data used during this record. equivalent to the list of RAs defined in TS 23.246 |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the BM-SC (network identifier part of APN) |
| Served PDP Address | O_C | Indicates the IP Multicast address used for the MBMS bearer context |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this MBMS bearer context, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. It shall include the required bearer capabilities (QoS Negotiated) |
| Record Opening Time | O_C | Time stamp when MBMS bearer context is activated in this SGSN or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the SGSN |
| Cause for Record Closing | O_C | The reason for the release of record from this SGSN |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number, only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| SGSN PLMN Identifier | O_C | SGSN PLMN Identifier (MCC and MNC) used during this record |
| Number of receiving UEs | O_C | Indicates the number of UEs receiving the MBMS bearer service |
| MBMS Information | O_C | MBMS related information related to MBMS bearer context being charged, defined in TS 32.273 |

#### 2.3.10 G-MB-CDR(GGSNMBMSRecord)

If the collection of CDR data is enabled then the GGSN data specified in the following table shall be available for each MBMS bearer context.

**Table 11: GGSN MBMS bearer context data (G-MB-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | GGSN MBMS bearer context record |
| GGSN Address used | O_C | The control plane IP address of the GGSN used |
| Charging ID | O_C | Bearer context identifier used to identify this MBMS bearer context in different records created by GSNs |
| List of Downstream Nodes | O_C | List of SGSN addresses used during this record. equivalent to the list of downstream nodes defined in TS 23.246 |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the BM-SC (network identifier part of APN) |
| Served PDP Address | O_C | Indicates the IP Multicast address used for the MBMS bearer context |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this MBMS bearer context, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. It shall include the required bearer capabilities (QoS Negotiated) |
| Record Opening Time | O_C | Time stamp when MBMS bearer context is activated in this SGSN or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the SGSN |
| Cause for Record Closing | O_C | The reason for the release of record from this SGSN |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number, only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| MBMS Information | O_C | MBMS related information related to MBMS bearer context being charged, defined in TS 32.273 |

#### 2.3.11 SGW-CDR(SGWRecord)

If FBC is disabled and the collection of CDR data is enabled then the S-GW data specified in the following table shall be available for each IP CAN bearer.

**Table 12: S-GW IP CAN bearer Data(SGW-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | S-GW IP CAN bearer record |
| Served IMSI | O_C | IMSI of the served party (non-anonymous connection) |
| Served IMEISV | O_C | IMEISV of the ME, if available |
| List of S-GW Address | O_C | The record is the control plane IP address of the S-GW used before consolidation, and IP CAN bearer-involved IP address list of S-GW used after consolidation |
| Charging ID | O_C | IP CAN bearer identifier used to identify this IP CAN bearer in different records created by PCNs |
| PDN Connection Id | O_C | This field holds the PDN connection (IP-CAN session) identifier to identify different records belonging to same PDN connection |
| Serving Node Address | O_C | List of serving node control plane IP addresses (e.g. SGSN, MME, …) used during this record |
| Serving Node Type | O_C | List of serving node types in control plane. The serving node types listed here map to the serving node addresses listed in the field "Serving node Address" in sequence |
| S-GW Change | O_C | Present if this is first record after S-GW change |
| PGW PLMN Identifier | O_C | PLMN identifier (MCC MNC) of the PGW used |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP/PDN Type | O_C | This field indicates PDN type (i.e IPv4, IPv6 or IPv4v6) |
| PDP/PDN Type extension | O_M | This field indicates Non-IP PDN type |
| Served PDP/PDN Address | O_C | IP address allocated for the PDP context / PDN connection, i.e. IPv4 or IPv6, if available. For non-IP PDN connection, this field is not present |
| Served PDP/PDN Address prefix length | O_C | PDP/PDN Address prefix length of an IPv6 typed Served PDP Address. The field needs not available for prefix length of 64 bits. For non-IP PDN connection, this field is not present |
| Dynamic Address Flag | O_C | Indicates whether served PDP/PDN address is dynamic, which is allocated during IP CAN bearer activation, initial attach (E-UTRAN or over S2x) and UE requested PDN connectivity. This field is missing if address is static |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this QCI/ARP pair, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are also listed |
| Record Opening Time | O_C | Time stamp when IP CAN bearer is activated in this S-GW or record opening time on subsequent partial records |
| MS Time Zone | O_C | This field contains the MS Time Zone the MS is currently located as defined in TS 29.060 [203], if available |
| Duration | O_C | Duration of this record in the S-GW |
| Cause for Record Closing | O_C | The reason for the release of record from this S-GW |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| List of Record Sequence Number | O_C | Partial record sequence number in this SGW. Only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| User Location Information | O_C | This field contains the User Location Information of the MS as defined in TS 29.060 [203] for GPRS case, and in TS 29.274 [210] for EPC case, if available |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP CAN bearer |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| IMS Signalling Context | O_C | Included if the IM-CN Subsystem Signalling Flag is set, see [201] IP CAN bearer is used for IMS signalling |
| P-GW Address used | O_C | This field is the P-GW IP Address for the Control Plane |
| Serving Node PLMN Identifier | O_C | Serving node PLMN Identifier (MCC and MNC) used during this record, if available |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station, when available. This RAT type is defined in TS 29.060 [204] for GTP case, in TS 29.274 [210] for eGTP case and in TS 29.275 [211] for PMIP case |
| Low Priority Indicator | O_C | This field indicates if this IP-CAN session has a low priority, i.e. for Machine Type Communication |
| Start Time | O_C | This field holds the time when User IP-CAN session starts, available in the CDR for the first bearer in an IP-CAN session |
| Stop Time | O_C | This field holds the time when User IP-CAN session is terminated, available in the CDR for the last bearer in an IP-CAN session |
| List of Local Record Sequence Number | O_C | Local sequence number of the original part of record and correspondent SGW address form a list, which occurs only in consolidated record |
| Consolidation Result | O_C | The consolidation result of partial record generated from one IP-CAN bearer., only occurs in consolidated record |
| Served PDP/PDN Address Extension | O_C | This field contains the IPv4 address for the PDN connection when dual-stack IPv4 IPv6 is used |
| IMSI Unauthenticated Flag | O_C | This field indicates the provided served IMSI is not authenticated (emergency bearer service situation) |
| User CSG Information | O_C | This field contains the "User CSG Information" status of the user accessing a CSG cell: it comprises CSG ID within the PLMN, Access mode and indication on CSG membership for the user when hybrid access applies |
| Dynamic Address Flag extension | O_C | Indicates whether served IPv4 PDP/PDN address is dynamic, which is allocated during IP-CAN bearer activation, initial attach and UE requested PDN connectivity with PDP/PDN type IPv4v6. This field is missing if IPv4 address is static |
| CP CIoT EPS Optimisation Indicator | O_C | This field indicates whether CP CIoT EPS Optimisation is used during data transfer with the UE (i.e. Control Plane NAS PDU via S11-U between SGW and MME) or not (i.e.User Plane via S1-U between SGW and eNB), if enabled |
| UNI PDU CP Only Flag | O_C | This field indicates whether this PDN connection is applied with "Control Plane Only Flag" for UNI PDU transfer, i.e. transfer only using Control Plane NAS PDU (Control Plane CIoT EPS optimisation) |
| Serving PLMN Rate Control | O_C | This field holds the Serving PLMN Rate Control used by the MME during this record |
| MO exception data counter | O_C | This field holds the MO exception data counter value with the timestamp indicating the time at which the counter value increased from 0 to 1, as defined in TS 29.274 [210] |
| List Of RAN Secondary RAT Usage Reports | O_C | This list applicable in SGW-CDR and PGW-CDR, includes one or more containers reported from the RAN for a secondary RAT |

#### 2.3.12 PGW-CDR(PGWRecord)

If FBC is enabled and the collection of CDR data is enabled then the P-GW data specified in the following table shall be available for each IP CAN bearer.

**Table 13: P-GW IP CAN bearer Data (PGW-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | P-GW IP CAN bearer record |
| Served IMSI | O_C | IMSI of the served party (non-anonymous connection) |
| Served IMEISV | O_C | IMEISV of the ME, if available |
| Served MN NAI | O_C | Mobile Node Identifier in NAI format (based on IMSI), if available |
| P-GW Address used | O_C | The control plane IP address of the P-GW used |
| Charging ID | O_C | IP CAN bearer identifier used to identify this IP CAN bearer in different records created by PCNs |
| PDN Connection Id | O_C | This field holds the PDN connection (IP-CAN session) identifier to identify different records belonging to same PDN connection |
| Serving Node Address | O_C | List of SGSN/S-GW control plane IP addresses used during this record |
| Serving Node Type | O_C | List of serving node types in control plane. The serving node types listed here map to the serving node addresses listed in the field "Serving node Address" in sequence |
| PGW PLMN Identifier | O_C | PLMN identifier (MCC MNC) of the PGW |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP/PDN Type | O_C | PDP type, i.e. IP, PPP, or IHOSS:OSP, or PDN type (i.e IPv4, IPv6 or IPv4v6) |
| PDP/PDN Type extension | O_M | This field indicates Non-IP PDN type |
| SGi PtP Tunnelling Method | O_C | This field indicates whether SGi PtP tunnelling method based on UDP/IP or other methods are used for this PDN connection when non-IP PDN connection |
| Served PDP/PDN Address | O_C | IP address allocated for the PDP context / PDN connection, i.e. IPv4 address when PDP/PDN Type is IPv4 or IPv6 prefix when PDP/PDN Type is IPv6 or IPv4v6, or assigned IP address if any when PDP/PDN Type extension is Non-IP. This parameter shall be present except: when both the PDP type is PPP and dynamic IP-CAN bearer address assignment is used or when PDP/PDN Type extension is Non-IP, and no IP address has been assigned by the PGW for the PDN connection |
| Served PDP/PDN Address prefix length | O_C | PDP/PDN Address prefix length of an IPv6 typed Served PDP Address. The field needs not available for prefix length of 64 bits |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this QCI/ARP pair, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are also listed |
| Dynamic Address Flag | O_C | Indicates whether served PDP/PDN address is dynamic, which is allocated during IP CAN bearer activation, initial attach (E-UTRAN or over S2x) and UE requested PDN connectivity. This field is missing if address is static |
| List of Service Data | O_C | A list of changes in charging conditions for all service data flows within this IP CAN bearer categorized per rating group or per combination of the rating group and service id. Each change is time stamped. Charging conditions are used to categorize traffic volumes, elapsed time and number of events, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are also listed |
| Record Opening Time | O_C | Time stamp when IP CAN bearer is activated in this P-GW or record opening time on subsequent partial records |
| MS Time Zone | O_C | This field contains the MS Time Zone the MS is currently located as defined in TS 29.060 [203], if available |
| Duration | O_C | Duration of this record in the P-GW |
| Cause for Record Closing | O_C | The reason for the release of record from this P-GW |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| List of Record Sequence Number | O_C | Partial record sequence number in this PGW. Only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| User Location Information | O_C | This field contains the User Location Information of the MS as defined in TS 29.060 [203] for GPRS case, and in TS 29.274 [210] for EPC case, if available |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP CAN bearer |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| IMS Signalling Context | O_C | Included if the IP CAN bearer IM-CN Subsystem Signalling Flag is set, see [201]is used for IMS signalling |
| External Charging Identifier | O_C | A Charging Identifier received from a non-EPC, external network entity e.g ICID |
| Serving Node PLMN Identifier | O_C | Serving node PLMN Identifier (MCC and MNC) used during this record |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station, when available. This RAT type is defined in TS 29.060 [204] for GTP case, in TS 29.274 [210] for eGTP case and in TS 29.275 [211] for PMIP case |
| Start Time | O_C | This field holds the time when User IP-CAN session starts, available in the CDR for the first bearer in an IP-CAN session |
| Stop Time | O_C | This field holds the time when User IP-CAN session is terminated, available in the CDR for the last bearer in an IP-CAN session |
| List of Local Record Sequence Number | O_C | Local sequence number of the original part of record and correspondent PGW address form a list, which occurs only in consolidated record |
| Consolidation Result | O_C | The consolidation result of partial record generated from one IP-CAN bearer., only occurs in consolidated record |
| Roaming Indicator | O_C | This field indicates the roaming CDR |
| DiameterSessionID | O_C | This field is used to associate PGW-CDR with CDR generated by OCS, this field is also used to judge whether the flag of OCS is inuse |
| Served PDP/PDN Address Extension | O_C | This field contains the IPv4 address for the PDN connection when dual-stack IPv4 IPv6 is used |
| IMSI Unauthenticated Flag | O_C | This field indicates the provided served IMSI is not authenticated (emergency bearer service situation) |
| 3GPP2 User Location information | O_C | This field contains the User Location Information of the MS as defined in TS 29.212 [71] for 3GPP2 access, if available |
| User CSG Information | O_C | This field contains the "User CSG Information" status of the user accessing a CSG cell: it comprises CSG ID within the PLMN, Access mode and indication on CSG membership for the user when hybrid access applies |
| PS Furnish Charging Information | O_C | This field contains charging information in case it is sent by OCS |
| TWAN User Location Information | O_C | This field holds the UE location in a Trusted WLAN Access Network (TWAN), i.e BSSID and SSID of the access point |
| EPC Qos Information | O_C | Contains the QoS applied for IP CAN bearer for EPC case |
| Dynamic Address Flag extension | O_C | Indicates whether served IPv4 PDP/PDN address is dynamic, which is allocated during IP-CAN bearer activation, initial attach and UE requested PDN connectivity with PDP/PDN type IPv4v6. This field is missing if IPv4 address is static |
| UWAN User Location Information | O_C | This field contains the UE location in an Untrusted Wireless Access Network (UWAN) which includes the UE local IP address and optionally UDP source port number (if NAT is detected) as defined in TS 29.274 [210]. It may also include WLAN location information (SSID and, when available, BSSID of the access point) the ePDG may have received from the 3GPP AAA server about the UE as defined in TS 29.274 [210] |
| Low Priority Indicator | O_C | This field indicates if this IP-CAN session has a low priority, i.e. for Machine Type Communication |
| UNI PDU CP Only Flag | O_C | This field indicates whether this PDN connection is applied with "Control Plane Only Flag" for UNI PDU transfer, i.e. transfer only using Control Plane NAS PDU (Control Plane CIoT EPS optimisation) |
| Serving PLMN Rate Control | O_C | This field holds the Serving PLMN Rate Control used by the MME during this record |
| APN Rate Control | O_C | This field holds the APN Rate Controls enforced in the PGW during this record |
| MO exception data counter | O_C | This field holds the MO exception data counter value with the timestamp indicating the time at which the counter value increased from 0 to 1, as defined in TS 29.274 [210] |
| SCS/AS Address | O_C | This field contains the Address of SCS/AS |
| List Of RAN Secondary RAT Usage Reports | O_C | This list applicable in SGW-CDR and PGW-CDR, includes one or more containers reported from the RAN for a secondary RAT |

#### 2.3.13 W-CDR(WLANRecord)

W-CDR is used to collect charging data of WLAN PDG.

**Table 14: WLAN PDG Record(W-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | WLAN PDG record |
| Served IMSI | O_C | IMSI of the served party |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| Served IMEISV | O_C | The IMEISV of the ME, if available |
| PDG Address used | O_C | IP address of the PDG used |
| Node ID | O_C | Name of the recording entity |
| Serving AAA Server/proxy Address | O_C | Serving AAA Server/Proxy addresses used during this record |
| WLAN UE remote IP address | O_C | WLAN UE remote IP address |
| Charging ID | O_C | PDG identifier used to correlate WLAN AN generated information to PDG generated information |
| WLAN session id | O_C | WLAN session identifier used to correlate WLAN AN generated information to PDG generated information |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| Charging Characteristics | O_C | The Charging Characteristics applied to the PDP context |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| Record Opening Time | O_C | Time stamp when End-to-end Tunnel is activated in this PDG? or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the PDG |
| Cause for Record Closing | O_C | The reason for the release of record from this PDG |
| List of Record Sequence Number | O_C | For those records that are partial record S/Ns generated in PDG before consolidation, and the S/N list of the original partial record after consolidation, the list includes PDG addresses |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| PDP Type | O_C | PDP type, i.e. IPv4, IPv6, or IHOSS:OSP |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this IP CAN bearer, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data volumes are also listed |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |

#### 2.3.14 HSGW-CDR(HSGWRecord)

HSGW-CDR is used to collect charging data of HSGW.

**Table 15: HSGW Record(HSGW-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | HSGW PDN Connection record |
| Served IMSI | O_C | IMSI of the served party, if available |
| IMSI Unauthenticated Flag | O_C | This field indicates the provided served IMSI is not authenticated (emergency bearer service situation) |
| Served IMEISV | O_C | IMEISV of the ME, if available |
| S-GW Address used | O_C | The IP address of the SGW used |
| S-GW Address IPv6 | O_C | This field contains the IPv6 address of the SGW used when dual-stack IPv4 IPv6 is used |
| Served 3GPP2 MEID | O_C | MEID of the served party's terminal equipment for 3GPP2 access |
| Charging ID | O_C | This field holds the Charging Id of the EPS default bearer in GTP case, or the unique Charging Id of the IP-CAN session in PMIP case: it is used to identify different records belonging to same PDN connection |
| Serving node Address | O_C | List of serving node IP addresses (i.e. ePCF) used during this record |
| Serving node IPv6 Address | O_C | This field contains the IPv6 address of the serving node address used when dual-stack IPv4 IPv6 is used |
| Serving node Type | O_C | List of serving node types in control plane. The serving node types listed here map to the serving node addresses listed in the field "Serving node Address" in sequence |
| S-GW Change | O_C | Present if this is first record after SGW change |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP/PDN Type | O_C | This field indicates PDN type (i.e IPv4, IPv6 or IPv4v6) |
| Served PDP/PDN Address | O_C | IP address allocated for the PDP context / PDN connection, if available, i.e. IPv4 when PDN Type is IPv4 or IPv6 when PDN Type is IPv6 or IPv4v6 |
| Served PDP/PDN Address Extension | O_C | This field holds IPv4 address of the served IMSI, if available, when PDN type is IPv4v6 |
| Dynamic Address Flag | O_C | Indicates whether served PDN address is dynamic, which is allocated during PDN connection activation, initial attach (eHRPD) and UE requested PDN connectivity. This field is missing if address IPv4is static when PDN Type is IPv4, or if IPv6 address is static when PDN Type is IPv6 or IPv4v6 |
| Dynamic Address Flag Extension | O_C | Indicates whether served IPv4 PDN address is dynamic, which is allocated duringPDN connection activation, initial attach (eHRPD) and UE requested PDN connectivity with PDP/PDN type IPv4v6. This field is missing if IPv4 address is static |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this PDN connection, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently corresponding data values are also listed |
| Record Opening Time | O_C | Time stamp when PDN Connection is activated in this HSGW or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the HSGW |
| Cause for Record Closing | O_C | The reason for the release of record from this HSGW |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number, only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| 3GPP2 User Location Information | O_C | This field contains the User Location Information of the MS (BSID or Subnet), if available |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP CAN bearer |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| P-GW Address used | O_C | This field is the P-GW IP Address for the Control Plane |
| P-GW IPv6 Address used | O_C | This field contains the IPv6 address of the P-GW address used when dual-stack IPv4 IPv6 is used |
| Serving Node PLMN Identifier | O_C | Serving node PLMN Identifier (MCC and MNC) used during this record, if available |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061 [205], when available |
| Start Time | O_C | This field holds the time when User IP-CAN session starts, available in the CDR for the first bearer in an IP-CAN session |
| Stop Time | O_C | This field holds the time when User IP-CAN session is terminated, available in the CDR for the last bearer in an IP-CAN session |

#### 2.3.15 MBMS-GW-CDR(GWMBMSRecord)

If the collection of CDR data is enabled then the MBMS GW data specified in the following table shall be available for each MBMS bearer context when MBMS GW doesn't locate in BM-SC.

**Table 16: MBMS-GW Record(MBMS-GW-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | MBMS GW MBMS bearer context record |
| MBMS GW Address used | O_C | The control plane IP address of the MBMS GW used |
| Charging ID | O_C | Bearer context identifier used to identify this MBMS bearer context in different records created by Evolved Packet System core network elements |
| List of Downstream Nodes | O_C | List of SGSN/MME addresses used during this record. Equivalent to the list of downstream nodes defined in TS 23.246 [207] |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the BM-SC (network identifier part of APN) |
| PDP/PDN Type | O_C | This field indicates PDN type (i.e IPv4 ,IPv6 or IPv4v6) |
| Served PDP/PDN Address | O_C | Indicates the IP Multicast address used for the MBMS bearer context. (i.e IPv4 or IPv6) |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this MBMS bearer context, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. It shall include the required bearer capabilities (QoS Negotiated) |
| Record Opening Time | O_C | Time stamp when MBMS bearer context is activated in this MBMS GW or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the MBMS GW |
| Cause for Record Closing | O_C | The reason for the release of record from this MBMS GW |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number, only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| MBMS Information | O_C | MBMS related information related to MBMS bearer context being charged, defined in TS 32.273 [32] |
| C-TEID | O_C | Common Tunnel Endpoint Identifier of MBMS GW for user plane, defined in TS23.246 [207] |
| IP multicast and Source address for distribution | O_C | IP addresses identifying the SSM channel used for user plane distribution on the backbone network defined in TS 23.246 [207] |

#### 2.3.16 ePDG-CDR(EPDGRecord)

If the collection of CDR data is enabled then the ePDG data specified in following table shall be available for each IP-CAN bearer.

**Table 17: ePDG IP-CAN bearer data (ePDG-CDR)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | ePDG IP-CAN bearer record |
| Served IMSI | O_C | IMSI of the served party, if available |
| ePDG Address Used | O_C | The control plane IP address of the ePDG used |
| Charging ID | O_C | IP-CAN bearer Charging identifier used to identify this IP-CAN bearer in different records created by PCNs |
| PDN Connection Charging Id | O_C | This field holds the Charging Id of the EPS default bearer in GTP case, or the unique Charging Id of the IP-CAN session in PMIP case: it is used to identify different records belonging to same PDN connection |
| SGW Change | O_C | Present if this is first record after a change from another serving node (i.e. SGW, ePDG) |
| PGW PLMN Identifier | O_C | PLMN identifier (MCC MNC) of the P-GW used |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP/PDN Type | O_C | This field indicates PDN type (i.e. IPv4, IPv6 or IPv4v6) |
| Served PDP/PDN Address | O_C | IP address allocated for the PDP context / PDN connection, if available, i.e. IPv4 address when PDN Type is IPv4 or IPv6 prefix when PDN Type is IPv6 or IPv4v6 |
| Served PDP/PDN Address extension | O_C | This field holds IPv4 address of the served IMSI, if available, when PDN type is IPv4v6 |
| Dynamic Address Flag | O_C | Indicates whether served PDP/PDN address is dynamic, which is allocated during IP-CAN bearer activation, initial attach and UE requested PDN connectivity. This field is missing if IPv4 address is static when PDN Type is IPv4, or if IPv6 address is static when PDN Type is IPv6 or IPv4v6 |
| Dynamic Address Flag extension | O_C | Indicates whether served IPv4 PDP/PDN address is dynamic, which is allocated during IP-CAN bearer activation, initial attach and UE requested PDN connectivity with PDP/PDN type IPv4v6. This field is missing if IPv4 address is static |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this QCI/ARP pair, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are also listed |
| Record Opening Time | O_C | Time stamp when IP-CAN bearer is activated in this ePDG or record opening time on subsequent partial records |
| Duration | O_C | Duration of this record in the ePDG |
| Cause for Record Closing | O_C | The reason for the release of record from this ePDG |
| Diagnostics | O_C | A more detailed reason for the release of the connection |
| Record Sequence Number | O_C | Partial record sequence number, only present in case of partial records |
| Node ID | O_C | Name of the recording entity |
| Local Record Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected |
| Served MSISDN | O_C | The primary MSISDN of the subscriber |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP-CAN bearer |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected |
| IMS Signalling Context | O_C | Included if the IM-CN Subsystem Signalling Flag is set, see TS 23.060 [201] IP-CAN bearer is used for IMS signalling |
| P-GW Address used | O_C | This field is the P-GW IP Address for the Control Plane |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061 [205], when available |
| Start Time | O_C | This field holds the time when User IP-CAN session starts, available in the CDR for the first bearer in an IP-CAN session |
| Stop Time | O_C | This field holds the time when User IP-CAN session is terminated, available in the CDR for the last bearer in an IP-CAN session |
| UWAN User Location Information | O_C | This field contains the UE location in an Untrusted Wireless Access Network (UWAN) which includes the UE local IP address and optionally UDP source port number (if NAT is detected) as defined in TS 29.274 [210]. It may also include WLAN location information (SSID and, when available, BSSID of the access point) the ePDG may have received from the 3GPP AAA server about the UE as defined in TS 29.274 [210] |

### 2.4 Description of Fields

This chapter is the brief description of each field of CDR mentioned above.

#### 2.4.1 3GPP2 User Location Information
The field is correspond to the ASN.1 field "threeGPP2UserLocationInformation".
This field contains the User Location Information of the MS (BSID or Subnet), if available.

#### 2.4.2 Access Point Name (APN) Network/Operator Identifier
The field is correspond to the ASN.1 field "accessPointNameNI/ accessPointNameOI".
These fields contain the actual connected Access Point Name (APN) Network/Operator Identifier determined either by MS, SGSN/MME or modified by CAMEL service. An APN can also be a wildcard, in which case the SGSN/MME selects the APN.

APN network identifier includes not just a flag, but also corresponds with the domain name of Internet.
APN operator identifier is composed of three labels. The first and second labels together shall uniquely identify the GPRS PLMN (e.g. "operator name>.<operator group>.gprs").

To represent the APN NI and OI in the PCN CDRs, the "dot" notation shall be used.

See 3GPP TS 23.003 [68] and 3GPP TS 23.060 [74] for more information about APN format and access point decision rules.

#### 2.4.3 APN Selection Mode
The field is correspond to the ASN.1 field "apnSelectionMode".
This field describes how the APN (selected by the SGSN/MME) to be used. The values and their meaning are as specified in Charter 7.9 'Information Elements' of 3GPP TS 29.060 clause.

#### 2.4.4 CAMEL Information
The field is correspond to the ASN.1 field "cAMELInformationMM/ cAMELInformationPDP/cAMELInformationSMS".
This field includes the following CAMEL information elements for S-CDR, M-CDR and S-SMO-CDR.

**CAMEL Access Point Name NI (S-CDR)**
This field contains the network identifier part of APN before modification by the SCF.

**CAMEL Access Point Name OI (S-CDR)**
This field contains the operator identifier part of APN before modification by the SCF.

**CAMEL Calling Party Number (S-SMO-CDR)**
This field contains the Calling Party Number modified by the CAMEL service.

**CAMEL Destination Subscriber Number (S-SMO-CDR)**
This field contains the short message Destination Number modified by the CAMEL service

**CAMEL SMSC Address (S-SMO-CDR)**
This field contains the SMSC address modified by the CAMEL service.

**SCF address (S-CDR, M-CDR, S-SMO-CDR)**
This field identifies the CAMEL server serving the subscriber. Address is defined in HLR as part of CAMEL subscription information.

**Service key (S-CDR, M-CDR, S-SMO-CDR)**
This field identifies the CAMEL service logic applied. Service key is defined in HLR as part of CAMEL subscription information.

**Default Transaction/SMS Handling (S-CDR, M-CDR, S-SMO-CDR)**
This field indicates whether or not a CAMEL encountered default GPRS- or SMS-handling. This field shall be present only if default call handling has been applied. Parameter is defined in HLR as part of CAMEL subscription information.

**Free Format Data (S-CDR, M-CDR, S-SMO-CDR)**
This field is contained in the CAP operation (defined in 3GPP TS 29.078, providing GPRS charging information (FCI)) sent by the gsmSCF. The data can be sent in one FCI message or several FCI messages with append indicator. This data is transferred transparently in the CAMEL session of the relevant call records.

If the FCI is received more then once during one CAMEL call, the append indicator defines whether the FCI information is appended to previous FCI and stored in the relevant record or the information of the last FCI received is stored in the relevant record (the previous FCI information shall be overwritten).

In the event of partial output, the currently valid "Free format data" is stored in the partial record.

**FFD Append Indicator (S-CDR, M-CDR)**
This field contains an indicator whether CAMEL free format data is to be appended to free format data stored in previous partial CDR. This field is needed in CDR post processing to sort out valid free format data for that call leg from sequence of partial records. Creation of partial records is independent of received FCIs and thus valid free format data may be divided to different partial records.

If field is missing then free format data in this CDR replaces all received free format data in previous CDRs. Append indicator is not needed in the first partial record. In following partial records indicator shall be true value if all FCIs received during that partial record have append indicator. If one or more of the received FCIs for that call leg during the partial record do not have append indicator then this field shall be missing.

**Level of CAMEL services (S-CDR, M-CDR)**
This field describes briefly the complexity of CAMEL invocation. Categories are the same as those of circuit switched services and measure of resource usage in VPLMN requested by HPLMN.
- "Basic" means that CAMEL feature is invoked during the PDP context activation phase only (e.g. to modify APN_NI/APN_OI)
- "Call duration supervision" means that PDP context duration or volume supervision is applied in the gprsSSF of the VPLMN (Apply Charging message is received from the gsmSCF)

**Number of DP encountered (S-CDR, M-CDR)**
This field indicates how long CAMEL detection point (TDP and EDP) encounters, balances signaling between VPLMNs and complements "Level of CAMEL service" field.

#### 2.4.5 Cause for Record Closing
The field is correspond to the ASN.1 field "causeForRecClosing".
This field contains a reason for the release of the CDR including the following:
- Normal release: PDP context release (end of context or SGSN change) or GPRS detach
- Partial record generation: data volume limit, time (duration) limit, maximum number of changes in charging conditions or intra SGSN intersystem change (change of radio interface from GSM to UMTS or vice versa)
- Abnormal termination (PDP or MM context)
- Unauthorized network originating a location service request
- Unauthorized client requesting a location service
- Location method failure at a location service execution
- Unknown or unreachable LCS client at a location service request
- Management intervention (request due to O&M reasons)
- Qos update by PCC

A more detailed reason may be found in the diagnostics field.

#### 2.4.6 Cell Identifier
The field is correspond to the ASN.1 field "cellIdentifier".
For GSM, the Cell Identifier is defined as the Cell Id, and for UMTS it is defined as the Service Area Code.

#### 2.4.7 Cell PLMN Identifier
The field is correspond to the ASN.1 field "cellPLMNID".
The field indicates MCC and MNC of the Cell at the time of Record Opening Time.

#### 2.4.8 Charging Characteristics
The field is correspond to the ASN.1 field "chargingCharacteristics".
The Charging Characteristics field allows the operator to apply different kind of charging methods in the CDRs. The format of charging characteristics field is as shown in the following figure.

**Figure 5: Charging Characteristics Flags**
```
Octet 1: [B4|B3|B2|B1|N|P|F|H]
Octet 2: [B12|B11|B10|B9|B8|B7|B6|B5]

H: Hot Billing = '00000001'B
F: Flat Rate = '00000010'B
P: Prepaid Service = '00000100'B
N: Normal Billing = '00001000'B
Bx (x=1..12): For specific behaviour defined on a per-Operator basis
```

The charging characteristics of S-CDR depends on SGSN, including the following points:
- If "PDP context Charging Characteristics" presents in the subscriber's data in the PDP context, it should be used
- If it does not appear, but "Subscribed Charging Characteristics" appears, "Subscribed Charging Characteristics" should be used

G-CDR charging characteristics are correspondent with "Charging characteristics" in GGSN PDP context.

#### 2.4.9 Charging Characteristics Selection Mode
The field is correspond to the ASN.1 field "chChSelectionMode".
This field indicates the charging characteristics type in GSN CDR.

In the SGSN the allowed values are:
- Home default
- Visiting default
- Roaming default
- APN specific
- Subscription specific

In the GGSN the allowed values are:
- Home default
- Visiting default
- Roaming default
- SGSN supplied

In the S-GW/P-GW the allowed values are:
- Home default
- Visiting default
- Roaming default
- Serving node supplied

#### 2.4.10 Charging ID
The field is correspond to the ASN.1 field "chargingID".
This field is a charging identifier, which can be used together with P-GW address to identify all records produced in SGSN(s), S-GW and P-GW involved in a single IP-CAN bearer. Charging ID is generated by P-GW at IP-CAN bearer activation and transferred to bearer requesting SGSN/S-GW. At inter-SGSN/S-GW change the charging ID is transferred to the new SGSN/S-GW as part of each active IP-CAN bearer. Different P-GWs allocate the charging ID independently of each other and may allocate the same numbers. The CGF and/or BS may check the uniqueness of each charging ID together with the P-GWs address and optionally (if still ambiguous) with the record opening time stamp.

#### 2.4.11 Consolidation Result
The field is correspond to the ASN.1 field "consolidationResult".
The field is the consolidation result flag of partial record that is generated in one PDP context. If one PHP context completes consolidation, set flag Normal, if some errors occur in consolidation, set flag NotNormal. If the length of the consolidated record exceeds the maximum length range of a record, set ReachLimit flag.

#### 2.4.12 CP CIoT EPS Optimisation Indicator
The field is correspond to the ASN.1 field "cPCIoTEPSOptimisationIndicator".
This field indicates whether CP CIoT EPS Optimisation is used during data transfer with the UE (i.e. Control Plane NAS PDU via S11-U between SGW and MME) or not (i.e.User Plane via S1-U between SGW and eNB), if enabled.

#### 2.4.13 C-TEID
The field is corresponded to the ASN.1 field "commonTeid".
This field contains common tunnel endpoint identifier of MBMS GW for user plane.

#### 2.4.14 Destination Number
The field is correspond to the ASN.1 field "BCDDirectoryNumber".
This field contains short message destination number requested by the user.

#### 2.4.15 Diagnostics
The field is correspond to the ASN.1 field "diagnostics".
This field includes a more detailed technical reason for the releases of the connection. See 3GPP TS 32.205. The diagnostics may also be extended to include manufacturer and network specific information.

#### 2.4.16 DiameterSessionID
The field is correspond to the ASN.1 field "diameterSessionID".
This field is used to associate G-CDR or PGW-CDR with CDR generated by OCS, this field is also used to judge whether the flag of OCS is inuse.

#### 2.4.17 Duration
The field is correspond to the ASN.1 field "duration".
This field contains the relevant duration in seconds for PDP contexts (S-CDR, G-CDR, and M-CDR). For some records, it is the duration of the CDRs of the parts instead of the cumulative duration of the call. The internal time measurements may be expressed in terms of tenths of seconds or even milliseconds and, as a result, the calculation of the duration may result in the rounding. Due to the following restrictions:
- Duration of zero second shall be accepted providing that the transferred data volume is greater than zero
- The same method of rounding shall be applied to all records

#### 2.4.18 Dynamic Address Flag
The field is correspond to the ASN.1 field "dynamicAddressFlag".
This field indicates that PDP address has been dynamically allocated for that particular PDP context. This field is missing if address is static.

#### 2.4.19 Dynamic Address Flag extension
The field is correspond to the ASN.1 field "dynamicAddressFlagExt".
This field indicates whether served IPv4 PDN address is dynamic, which is allocated duringPDN connection activation, initial attach (eHRPD) and UE requested PDN connectivity with PDP/PDN type IPv4v6. This field is missing if IPv4 address is static.

#### 2.4.20 EPC Qos Information
The field is correspond to the ASN.1 field "ePCQoSInformation".
This field contains the QoS applied for IP CAN bearer during the service data container recording interval for EPC case.

#### 2.4.21 ePDG Address Used
This field is the serving ePDG IP Address for the Control Plane. If both an IPv4 and an IPv6 address of the ePDG is available, the ePDG shall include the IPv4 address in the CDR.

#### 2.4.22 Event Time Stamps
The field is correspond to the ASN.1 field "eventTimeStamp/originationTime".
These fields contain the event time stamps relevant for each of the individual record types. A time stamp shall contain year, month, date, hour, minute, and second at least.

#### 2.4.23 External Charging Identifier
The field is correspond to the ASN.1 field "externalChargingID".
A Charging Identifier received from a none-GPRS, external network entity. When inter-working with IMS the external charging identifier is the ICID (IMS Charging IDentifier) as received from the IMS network by the GGSN.

#### 2.4.24 GGSN Address/GGSN Address Used
The field is correspond to the ASN.1 field "ggsnAddress/ggsnAddressUsed".
These fields are the current serving GGSN IP Address for the Control Plane.

#### 2.4.25 HSGW Address used
The field is correspond to the ASN.1 field "hSGWAddressUsed".
This field indicates the IP address of the HSGW used.

#### 2.4.26 HSGW Change
The field is correspond to the ASN.1 field "hSGWChange".
The field is presented if this is first record after HSGW change.

#### 2.4.27 IMS Signalling Context
The field is correspond to the ASN.1 field "iMSsignalingContext".
Indicates if the PDP context is used for IMS signalling. It is only present if the PDP context is an IMS signalling PDP context. A PDP context for IMS signalling is determined via the "IM CN Subsystem Signalling Flag" conveyed via the "Activate PDP context request" message from the MS to the network.

#### 2.4.28 IMSI Unauthenticated Flag
The field is correspond to the ASN.1 field "iMSIunauthenticatedFlag".
This field indicates the provided served IMSI is not authenticated (emergency bearer service situation).

#### 2.4.29 IP multicast and Source address for distribution
The field is correspond to the ASN.1 field "iPMulticastSourceAddress".
The field contains IP addresses identifying the SSM channel used for user plane distribution on the backbone network defined in TS 23.246.

#### 2.4.30 LCS Cause
The field is correspond to the ASN.1 field "lcsCause".
The LCS Cause parameter provides the reason for an unsuccessful location request according to 3GPP TS 49.031.

#### 2.4.31 LCS Client Identity
The field is correspond to the ASN.1 field "lcsClientIdentity".
This field contains further information on the LCS Client identity:
- Client External ID
- Client Dialed by MS ID
- Client Internal ID

#### 2.4.32 LCS Client Type
The field is correspond to the ASN.1 field "lcsClientType".
This field contains the type of the LCS Client as defined in 3GPP TS 29.002.

#### 2.4.33 LCS Priority
The field is correspond to the ASN.1 field "lcsPriority".
This parameter gives the priority of the location request as defined in 3GPP TS 49.031.

#### 2.4.34 LCS QoS
The field is correspond to the ASN.1 field "lcsQos".
This information element defines the Quality of Service for a location request as defined in 3GPP TS 49.031.

#### 2.4.35 List of Downstream Nodes
The field is correspond to the ASN.1 field "listofDownstreamNodes".
List of SGSN addresses used during this record. equivalent to the list of downstream nodes defined in TS 23.246.

#### 2.4.36 List of RAN Secondary RAT Usage Reports
The field is correspond to the ASN.1 field "listOfRANSecondaryRATUsageReports".
This list applicable in SGW-CDR and PGW-CDR, includes one or more containers reported from the RAN for a secondary RAT.

Each container includes the following fields:
- **Data Volume Uplink**: includes the number of octets transmitted during the use of the packet data services in the uplink direction reported from RAN
- **Data Volume Downlink**: includes the number of octets transmitted during the use of the packet data services in the downlink direction reported from RAN
- **RAN Start Time**: is a time stamp, which defines the moment when the volume container is opened by the RAN
- **RAN End Time**: is a time stamp, which defines the moment when the volume container is closed by the RAN
- **Secondary RAT Type**: This field contains the RAT type for the secondary RAT

#### 2.4.37 List of RAs
The field is correspond to the ASN.1 field "listofRAs".
List of routeing areas receiving data used during this record. equivalent to the list of RAs defined in TS 23.246.

#### 2.4.38 List of Service Data

This list includes one or more service data containers. Depending the reporting level of PCC rules one service data container either includes charging data for one rating group or for one rating group and service id combination. Each service data container may include the following fields:

- **AF-Record-Information**: includes the "AF Charging Identifier" (ICID for IMS) and associated flow identifiers generated by the AF and received by the P-GW over Gx interfaces as defined in TS 29.212 [220]. In case usage of PCC rules, which usage is reported within the container, has different AF-Record-Information then the P-GW shall include only one occurrence to the service data container.

- **Charging Rule Base Name**: is the reference to group of PCC rules predefined at the PCEF. This field is included if any of the PCC rules, which usage is reported within this service data container, was activated by using the Charging Rule Base Name as specified in TS 29.212 [220]. In case multiple Charging Rule Base Names activate PCC rules, which usage is reported within this service data container, the P-GW shall include only one occurrence to the service data container.

- **Data Volume Downlink/Uplink**: includes the number of octets transmitted during the service data container recording interval in the uplink and/or downlink direction, respectively.

- **Local Sequence Number**: is a service data container sequence number. It starts from 1 and is increased by 1 for each service date container generated within the lifetime of this IP-CAN bearer.

- **Qos Information**: in IP CAN bearer specific service data container contains the negotiated QoS applied for the IP CAN bearer. QoS Information in service specific service data containers contains the QoS applied for the service. This is included in the first service data container. In following container QoS information is present if previous change condition is "QoS change". The P-GW shall include only one QoS Information occurrence to one service data container.

- **Rating Group**: is the identifier of rating group. This field is mandatory. The parameter corresponds to the Charging Key as specified in TS 23.203 [87].

- **Report Time**: is a time stamp, which defines the moment when the service data container is closed.

- **Result Code**: contains the result code after the interconnection with the OCS. This field may be added to the service data container if online and offline charging are both used for same rating group. The result code in service data container is the value of the Result-Code AVP received within last CCA message in corresponding MSCC AVP to this service data container.

- **Service Condition Change**: defines the reason for closing the service data container (see TS 32.251 [11]), such as tariff time change, IP-CAN bearer modification(e.g. QoS change, S-GW change, user location change), service usage thresholds, service idled out, termination or failure handling procedure. When one of the "CGI/SAI, ECGI or TAI or RAI Change" are reported as user location change, the dedicated value in service Condition Change is set instead of the generic "user location change" value. This field is specified as bitmask for support of multiple change trigger (e.g. S-GW and QoS change). This field is derived from Change-Condition AVP at Service-Data-Container AVP level defined in TS 32.299 [40] when received on Rf. Each value is mapped to the corresponding value in "ServiceConditionChange" field. When simultaneous change triggers are met, multiple Change-Condition values are set in field bitmask. When no Change-Condition AVP is provided, the "recordClosure" value is set for the service data container. For envelope reporting, the Service Condition Change value shall always take the value "envelopeClosure". The mechanism for creating the envelope is identified within the Time Quota Mechanism field.
Note: After this release, you can use the "Service Condition Change Extensions" field instead of this field. And the "Service Condition Change" will be compatible with the previous version.

- **Service Identifier**: is an identifier for a service. The service identifier may designate an end user service, a part of an end user service or an arbitrarily formed group thereof. This field is only included if reporting is per combination of the rating group and service id.

- **Serving Node Address**: contains the valid serving node (e.g.SGSN/S-GW) control plane IP address during the service data container recording interval.

- **Time of First Usage**: is the time stamp for the first IP packet to be transmitted and mapped to the current service data container. For envelope reporting controlled by the Time Quota Mechanism, this indicates the time stamp for the first IP packet to be transmitted that causes an envelope to be opened – see TS 32.299 [40].

- **Time of Last Usage**: is the time stamp for the last IP packet to be transmitted and mapped to the current service data container. For envelope reporting, controlled by the Time Quota Mechanism, this indicates the time stamp for an envelope to be closed – see TS 32.299 [40] for conditions for envelope closure.

- **Time Usage**: contains the effective used time within the service data container recording interval.

- **eventCounter**: is the event successful counter.

- **l7UpVolume/l7DownVolume**: is data volume uplink/downlink of this level 7 service (Unit: bytes)

- **attemptCounter**: is the event attempt counter.

- **User location information**: contains the user location (e.g. CGI/SAI, ECGI/TAI or RAI) where the UE was located during the service data container recording interval. This is included in the service data container only if previous container's change condition is "user location change" or one of the "CGI/SAI, ECGI or TAI or RAI Change". Note the user location information in PGW-CDR main level contains the location where the UE was when PGW-CDR was opened.

- **serviceChargeType**: indicates volume or time quota type in CDR. The quota type comes from CCA in online charging.

- **threeGPP2UserLocationInformation**: holds the 3GPP2 User Location Information. It contains the 3GPP2-BSID as described in TS 29.212 [220]. The parameter is provided to the PGW during IP-CAN session establishment/modification, through PCC procedures for non-3GPP Accesses, as defined in TS 23.203 [203].

- **Sponsor Identity**: identifies the sponsor willing to pay for the operator's charge for connectivity.

- **Application Service Provider Identity**: identifies application service provider that is delivering a service to an end user.

- **UWAN User Location Information**: contains the UE location in an Untrusted Wireless Access Network (UWAN) which includes the UE local IP address and optionally UDP source port number (if NAT is detected) as defined in TS 29.274 [210]. It may also include WLAN location information (SSID and, when available, BSSID of the access point) the ePDG may have received from the 3GPP AAA server about the UE as defined in TS 29.274 [210].

- **Serving PLMN Rate Control**: holds the Serving PLMN Rate Control used by the MME during this record.

- **APN Rate Control**: holds the APN Rate Controls enforced in the PGW during this record.

- **URL**: This field indicates the first visited URL for this service of the subscriber.

- **Service Condition Change Extensions**: is the extensions of Serving Condition Change. If you want to use "NB-IOT" function, please use this field instead of "Service Condition Change".

#### 2.4.39 List of SGSN Address
The field is correspond to the ASN.1 field "sgsnAddressList".
This list includes one or more SGSN IP addresses list used during this record.

#### 2.4.40 List of Traffic Data Volumes
The field is correspond to the ASN.1 field "listOfTrafficVolumes".

This list includes one or more containers and each includes the following fields:

**Data Volume Uplink** includes the number of octets transmitted during the use of the packet data services in the uplink direction. In MBMS charging, this field is normally to be set to zero, because MBMS charging is based on the volume of the downlink data. This field is set to zero when the SGSN has successfully established Direct Tunnel between the RNC and the GGSN.

**Data Volume Downlink** includes the number of octets transmitted during the use of the packet data services in the downlink direction. This field is set to zero when the SGSN has successfully established Direct Tunnel between the RNC and the GGSN.

**Change Condition** defines the reason for closing the container, such as tariff time change, QoS change or closing of the CDR.

**Change Time** is a time stamp, which defines the moment when the volume container is open or the CDR is closed. All the active PDP contexts do not need to have exactly the same time stamp e.g. due to some charge rate change.

**Quality of Service Requested** contains the QoS desired by MS at PDP context activation, **QoS Negotiated** indicates the applied QoS accepted by the network.

**User Location Information** contains the user location (e.g. CGI/SAI, ECGI/TAI or RAI) where the UE was located during the use of the packet data services for EPC case.

**EPC QoS Information** contains the QoS applied for IP CAN bearer during the service data container recording interval for EPC case.

**UWAN User Location Information** contains the UE location in an Untrusted Wireless Access Network (UWAN) which includes the UE local IP address and optionally UDP source port number (if NAT is detected) as defined in TS 29.274 [210]. It may also include WLAN location information (SSID and, when available, BSSID of the access point) the ePDG may have received from the 3GPP AAA server about the UE as defined in TS 29.274 [210].

**CP CIoT EPS Optimisation Indicator** indicates whether CP CIoT EPS Optimisation is used during data transfer with the UE (i.e. Control Plane NAS PDU via S11-U between SGW and MME) or not (i.e.User Plane via S1-U between SGW and eNB), if enabled.

**Serving PLMN Rate Control** holds the Serving PLMN Rate Control used by the MME during this record.

First container includes following optional fields: QoS Requested (not in G-CDR) and QoS Negotiated. In following containers QoS Negotiated is present if previous change condition is "QoS change". In addition to the QoS Negotiated parameter the QoS Requested parameter is present in following containers, if the change condition is "QoS change" and the QoS change was initiated by the MS via a PDP context modification procedure.

If a pre-Release '99 only capable terminal is served, the QoS profile consists of five attributes as follows: reliability, delay, precedence, peak throughput and mean throughput. Encoding of this QoS profile shall be in accordance with GSM 12.15.

In Release 99, the QoS profile consists of the above 2G parameters plus the following UMTS attributes: Traffic class ("conversational", "streaming", "interactive", "background"), Maximum bit-rate (kbps), Delivery order (y/n), Maximum SDU size (octets), SDU error ratio, Residual bit error ratio, Delivery of erroneous SDUs (y/n/-), Transfer delay (ms), Traffic handling priority, Allocation/Retention Priority. This QoS profile shall be encoded according to the "Quality of Service (QoS) Profile" parameter specified in 3GPP TS 29.060.

This table illustrates an example of a list, which has three containers, caused by one QoS change and one tariff time change.

**Table 18: Example List of Traffic Data Volumes**

| Container 1 | Container 2 | Container 3 |
|------------|-------------|-------------|
| QoS Requested = QoS1 | | |
| QoS Negotiated = QoS1 | QoS Negotiated = QoS2 | |
| Data Volume Uplink = 1 | Data Volume Uplink = 5 | Data Volume Uplink = 3 |
| Data Volume Downlink = 2 | Data Volume Downlink = 6 | Data Volume Downlink = 4 |
| Change Condition = QoS change | Change Condition = Tariff change | Change Condition = Record closed |
| Time Stamp = TIME1 | Time Stamp = TIME2 | Time Stamp = TIME3 |

First container includes initial QoS values and corresponding volume counts. Second container includes new QoS values and corresponding volume counts before tariff time change. Last container includes volume counts after the tariff time change. The total volume counts can be itemized as shown in Table 19 (tariff1 is used before and tariff2 after the tariff time change):

**Table 19: QoS Value and Total Volume Count**

| Description | Value | Container |
|-------------|-------|-----------|
| QoS1+Tariff1 | uplink = 1, downlink = 2 | 1 |
| QoS2+Tariff1 | uplink = 5, downlink = 6 | 2 |
| QoS2+Tariff2 | uplink = 3, downlink = 4 | 3 |
| QoS1 | uplink = 1, downlink = 2 | 1 |
| QoS2 | uplink = 8, downlink = 10 | 2+3 |
| Tariff1 | uplink = 6, downlink = 8 | 1+2 |
| Tariff2 | uplink = 3, downlink = 4 | 3 |

The volume of data counted in the GGSN shall be the volume transited on GTP layer. Therefore the data counted already includes the IP PDP bearer protocols, i.e. IP TCP or UDP.

The data volume counted in the SGSN is the data volume on SNDCP PDUs layer. Therefore, the data counted already includes the headers of any PDP bearer protocols.

The amount of data counted in the 3G-GGSN shall be the volume transited on GTP-U layer. Therefore, the data counted already includes the headers of any PDP bearer protocols.

The amount of data counted in the 3G-SGSN shall be the data volume transmitted on GTP-U PDUs layer. Therefore, the data counted already includes the headers of any PDP bearer protocols.

In GSM, in order to avoid that downstream packets transmitted from the old SGSN to the new SGSN at inter SGSN RA update, induce the increase of the PDP CDR downstream volume counters in both SGSN, the following rules must be followed:
- For PDP contexts using LLC in unacknowledged mode: an SGSN shall update the PDP CDR when the packet has been sent by the SGSN towards the MS.
- For PDP contexts using LLC in acknowledged mode, a 2G-SGSN shall only update the PDP CDR at the reception of the acknowledgement by the MS of the correct reception of a downstream packet. In other worlds, for inter SGSN RA update, the new SGSN shall update the PDP CDR record when a downstream packet sent by the old SGSN is received by the MS and acknowledged by the MS towards the new SGSN through the RA update complete message. In UMTS, the not transferred downlink data can be accounted for in the S-CDR with "RNC Unsent Downlink Volume" field, which is the data that the RNC has discarded or forwarded during handover.

In UMTS network, the not transferred downlink data can be accounted for in the S-CDR with "RNC Unsent Downlink Volume" field, which is the data that the RNC has discarded or forwarded during handover.

Data volumes (accounted by RLC or LLC) retransmitted due to poor radio link conditions shall not be counted.

#### 2.4.41 Local Sequence Number /List of Local Record Sequence Number
The field is correspond to the ASN.1 field "localSequenceNumber/localSeqNoList /chgLocalSeqNoList".
This field includes a unique record number created by this node. The number is unique within one node, which is identified by field Node ID or record-dependent node address (SGSN address, GGSN address, Recording Entity).The field can be used, for example, to identify missing records in post processing system.

List of Local Record Sequence Number include local sequence number of the original partial record and correspondent SGSN address form a list, which occurs only in consolidated record.

#### 2.4.42 Location Estimate
The field is correspond to the ASN.1 field "locationEstimate".
The Location Estimate field is providing an estimate of a geographic location of a target MS according to 3GPP TS 29.002.

#### 2.4.43 Location Method
The field is correspond to the ASN.1 field "locationMethod".
The Location Method identifier refers to the argument of LCS-MOLR that was invoked as defined in 3GPP TS 24.080.

#### 2.4.44 Location Type
The field is correspond to the ASN.1 field "locationType".
This field contains the type of the location as defined in 3GPP TS 29.002.

#### 2.4.45 MBMS GW Address used
The field is correspond to the ASN.1 field "mbmsGWAddress".
This parameter holds the IP-address of the MBMS GW that generated the Charging Id when MBMS GW is stand-alone.

#### 2.4.46 MBMS Information
The field is correspond to the ASN.1 field "mbmsInformation".
MBMS related information related to MBMS bearer context being charged and each includes the following fields:

**TMGI** - The field contains the Temporary Mobile Group Identity allocated to a particular MBMS bearer service. TMGI use and structure is specified in 3GPP TS 23.003.

**MBMS Session Identity** - This field together with TMGI identifies a transmission of a specific MBMS session.

**MBMS Service Type** - The field is used to indicate the type of MBMS bearer service: multicast or broadcast.

**MBMS 2G 3G Indicator** - The MBMS 2G 3G Indicator is used to indicate the radio access type that can receive the MBMS bearer service.

**MBMS Service Area** - The field indicates the area over which the MBMS bearer service has to be distributed.

**Required MBMS Bearer Capabilities** - The field contains the minimum bearer capabilities the UE needs to support.

**MBMS GW Address** - This field holds the IP-address of the MBMS GW that generated the Charging Id when MBMS GW is stand-alone.

**CN IP Multicast Distribution** - This field is used to indicate if IP multicast distribution to UTRAN is used for the MBMS user plane data.

**MBMS Access Indicator** - This field indicates whether the MBMS bearer service will be delivered in UTRAN-only, E-UTRAN-only or both coverage areas.

#### 2.4.47 Measurement Duration
The field is correspond to the ASN.1 field "measurementDuration".
This field contains the duration for the section of the location measurement corresponding to the Perform_Location_Request and Perform_Location_Response by the SGSN.

#### 2.4.48 Message reference
The field is correspond to the ASN.1 field "messageReference".
This field contains a unique message reference number allocated by the Mobile Station (MS) when transmitting a short message to the service centre. This field corresponds to the TP-Message-Reference element of the SMS_SUBMIT PDU defined in 3GPP TS 23.040.

#### 2.4.49 MLC Number
The field is correspond to the ASN.1 field "mlcNumber".
This parameter refers to the ISDN (E.164) number of an MLC.

#### 2.4.50 MS Network Capability
The field is correspond to the ASN.1 field "msNetworkCapability".
This MS Network Capability field contains the MS network capability value of the MS network capability information element of the served MS on PDP context activation or on GPRS attachment as defined in 3GPP TS 24.008.

#### 2.4.51 MS Time Zone
The field is correspond to the ASN.1 field "mSTimeZone".
This field contains the 'Time Zone' IE provided by the SGSN and transferred to the GGSN during the PDP context activation/modification procedure as specified in TS 29.060.

#### 2.4.52 Network Initiated PDP Context
The field is correspond to the ASN.1 field "networkInitiation".
This field indicates that PDP context is network initiated. The field is missing in case of mobile activated PDP context.

#### 2.4.53 Node ID
The field is correspond to the ASN.1 field "nodeID".
This field contains an optional, operator configurable identifier string for the node that is generated from CDR.

#### 2.4.54 Notification to MS User
The field is correspond to the ASN.1 field "notificationToMSUser".
This field contains the privacy notification to MS user that was applicable when the LR was invoked as defined in TS 29.002.

#### 2.4.55 Number of receiving UEs
The field is correspond to the ASN.1 field "numberofReceivingUE".
This field Indicates the number of UEs receiving the MBMS bearer service.

#### 2.4.56 PDN Connection ID
The field is correspond to the ASN.1 field "pDNConnectionID".
For SGW and PGW, this field indicates a PDN connection charging identifier. This field can be used together with P-GW address to identify all records involved in an IP CAN bearer (PDN connection).
For HSGW, this field indicates PDN Connection Id for MUPSAP.

#### 2.4.57 PDP/PDN Type
The field is correspond to the ASN.1 field "pdpType".
This field defines the bearer type, e.g. IP, PPP, or IHOSS:OSP. See:
- TS 29.060 [75] for exact format of PDP type for GTP case
- TS 29.274 [91] for exact format of PDN type for eGTP
- TS 29.275 [92] for exact format of PDN type for PMIP

#### 2.4.58 Positioning Data
The field is correspond to the ASN.1 field "positioningData".
This information element is providing positioning data associated with a successful or unsuccessful location attempt for a target MS according TS49.031.

#### 2.4.59 Privacy Override
The field is correspond to the ASN.1 field "privacyOverride".
This parameter indicates if the LCS client overrides MS privacy when the GMLC and VMSC/SGSN for an MT-LR are in the same country as defined in 3GPP TS 29.002.

#### 2.4.60 P-GW Address Used
The field is correspond to the ASN.1 field "p-GWAddressUsed".
These field is the serving P-GW IP Address for the Control Plane. If both an IPv4 and an IPv6 address of the P-GW is available, the P-GW shall include the IPv4 address in the CDR.

#### 2.4.61 P-GW IPv6 Address used
The field is correspond to the ASN.1 field "p-GWAddressUsedIPv6".
This field contains the IPv6 address for the P-GW address when dual-stack IPv4 IPv6 is used.

#### 2.4.62 P-GW PLMN Identifier
The field is correspond to the ASN.1 field "p-GWPLMNIdentifier".
This field contains P-GW PLMN Identifier (Mobile Country Code and Mobile Network Code).

#### 2.4.63 PS Furnish Charging Information
The field is correspond to the ASN.1 field "pSFurnishChargingInformation".
This field contains charging information in case it is sent by OCS, and it contains PS Free Format Data and PS FFD Append Indicator. (see 3GPP TS 32.298).

#### 2.4.64 RAT Type
The field is correspond to the ASN.1 field "rATType".
Holds the value of RAT Type, as provided to S-GW and P-GW, described in:
- TS 29.060 [75] for GTP case
- TS 29.274 [91] for eGTP case
- TS 29.275 [92] for PMIP case

The field is provided by the SGSN/MME and transferred to the S-GW/P-GW during the IP-CAN bearer activation/modification.

#### 2.4.65 Record Extensions
The field is correspond to the ASN.1 field "recordExtensions".
This field enables network operators or manufacturers to add their own recommended extensions to the standard record definitions.

When GGSN supports content charging, GGSN equipment of manufacturers should fill the content charging information generated from network side into the parameters of the G-CDR. This parameter contains a list, this list includes one or more containers and each includes the following fields:

- **serviceCode**: Service code
- **upVolume/downVolume**: data volume uplink/downlink of this level 3 service (Unit: bytes)
- **qosNegotiated**: the negotiated QoS applied for this service
- **usageDuration**: the effective used time within this service
- **ratingGroup**: the service flow identity
- **resultCode**: contains the result code
- **timeOfFirstUsage**: the time stamp for the first IP packet to be transmitted for the service data flow
- **timeOfLastUsage**: the time stamp for the last IP packet to be transmitted for the service data flow
- **serviceConditionChange**: defines the reason for closing the service data container
- **timeOfReport**: a time stamp, which defines the moment when the service data container is closed
- **failureHandlingContinue**: included when the failure handling procedure has been executed
- **eventCounter**: the event successful counter
- **l7UpVolume/l7DownVolume**: data volume uplink/downlink of this level 7 service (Unit: bytes)
- **attemptCounter**: the event attempt counter
- **serviceChargeType**: indicates volume or time quota type in CDR. The quota type comes from CCA in online charging
- **User location information**: contains the user location where the UE was located during the service data container recording interval for EPC case
- **EPC QoS Information**: contains the QoS applied for IP CAN bearer during the service data container recording interval for EPC case
- **EPC User Location Information**: contains the user location (e.g. CGI/SAI, ECGI/TAI or RAI) where the UE was located during the service data container recording interval for EPC case
- **URL**: This field indicates the first visited URL for this service of the subscriber

**Table 20: Field Definition of G-CDR Content Charging Information**

| Field Name | Data Type | Description |
|------------|-----------|-------------|
| extensionType | Unsigned char | The type of extension field takes the values as follows: 1:Content charging, Others: Reserved |
| contentInfo | Structure | Service data container |
| service data container 1 | Structure | Service data container |
| service data container 2 | Structure | Service data container |
| ... | ... | ... |
| service data container N | Structure | Service data container |

#### 2.4.66 Record Opening Time
The field is correspond to the ASN.1 field "recordOpeningTime".
This field contains the time stamp that records the opening time (see 3GPP TS 32.005 for exact format).

#### 2.4.67 Record Sequence Number/List of Record Sequence Number
The field is correspond to the ASN.1 field "recordSequenceNumber /listOfRecordSequenceNumber".
This field contains a running sequence number employed to link the partial records generated in the SGSN/GGSN for a particular PDP context (characterized with the same Charging ID and GGSN address pair). For S-CDR, the sequence number always restarts from one (1) when an inter-SGSN routing area updates, see field "SGSN change". The Record Sequence Number is missing if the record is the only one produced in the SGSN/GGSN for a PDP context (e.g. inter-SGSN routing area update can result in two S-CDRs without sequence number and field "SGSN change" present in the second record).

List of Record Sequence Number include record sequence of the original partial record after consolidation, the list includes GGSN addresses.

#### 2.4.68 Record Type
The field is correspond to the ASN.1 field "recordType".
The field identifies the type of the record e.g. S-CDR, SGW-CDR, PGW-CDR, M-CDR, S-SMO-CDR and S-SMT-CDR.

#### 2.4.69 Recording Entity Number
The field is correspond to the ASN.1 field "recordingEntity".
This field contains the ITU-T E.164 number assigned to the entity that produced the record. For further details see 3GPP TS 23.003.

#### 2.4.70 RNC Unsent Downlink Volume
The field is correspond to the ASN.1 field "rNCUnsentDownlinkVolume".
This field contains the unsent downlink volume that the RNC has discarded or forwarded to 2G-SGSN and already included in S-CDR. This field is present when RNC has provided unsent downlink volume counts at RAB release and can be used by a downstream system to apply proper charging.

#### 2.4.71 Roaming Indicator
The field is correspond to the ASN.1 field "roamingIndicator".
This field indicates the roaming CDR.

#### 2.4.72 Routing Area Code/Cell Identifier/Change of location
The field is correspond to the ASN.1 field "location/locationArea/locationAreaCode/routingArea/changeLocation".
The location information contains a combination of the Routing Area Code (RAC) and an optional Cell Identifier of the routing area and cell in which the served party is currently located. In 2G domains, the Cell Identifier is defined by the Cell Identity (CI) and in 3G domains by the Service Area Code (SAC). Any change of location (i.e. Routing Area change) may be recorded in the change of location field including the time at which the change took place. The field of location change is optional and not required if partial records are generated when the location changes.

The RAC and (optionally) CI are coded according to 3G TS 24.008 and the SAC according to 3GPP TS 25.413.

#### 2.4.73 Served 3GPP2 MEID
The field is correspond to the ASN.1 field "served3gpp2MEID".
This field indicates MEID of the served party's terminal equipment for 3GPP2 access.

#### 2.4.74 Served IMEI
The field is correspond to the ASN.1 field "servedIMEI".
This field contains the International Mobile Equipment Identity (IMEI) of the terminal equipment served. The field is used to describe the ME involved in the recorded transaction e.g. the called ME in the case of a network initiated PDP context. The format of the IMEI is defined in 3GPP TS 23.003.

#### 2.4.75 Served IMEISV
The field is correspond to the ASN.1 field "servedIMEISV".
This field contains the International Mobile Equipment Identity and Software Version Number (IMEISV) and is defined in 3GPP TS 23.003.

#### 2.4.76 Served IMSI
The field is correspond to the ASN.1 field "servedIMSI".
This field contains the International Mobile Subscriber Identity (IMSI) of the served party. The field is used to describe the mobile subscriber involved in the recorded transaction, e.g. the calling subscriber in case of a mobile initiated PDP context. The format of the IMSI is defined in 3GPP TS 23.003.

#### 2.4.77 Served MN NAI
The field is correspond to the ASN.1 field "servedMNNAI".
This field contains the International Mobile Subscriber Identity (IMSI) of the served party in the form of a Network Access Identifier (NAI) and is defined in 3GPP TS 23.003.

#### 2.4.78 Served MSISDN
The field is correspond to the ASN.1 field "servedMSISDN".
This field contains the Mobile Station (MS) ISDN number (MSISDN) of the served party. The field is used to describe the mobile subscriber involved in the transaction. The format of the MSISDN is defined in 3GPP TS 23.003.

#### 2.4.79 Served PDP/PDN Address
The field is correspond to the ASN.1 field "servedPDPAddress/servedPDPPDNAddress".
This field contains the IP address for the PDN connection (PDP context, IP-CAN bearer). This is a network layer address i.e. of type IP version 4 or IP version 6. The address for each Bearer type is allocated either temporarily or permanently (see "Dynamic Address Flag"). This parameter shall be present except when both the Bearer type is PPP and dynamic address assignment is used.

#### 2.4.80 Served PDP/PDN Address Extension
The field is correspond to the ASN.1 field "servedPDPPDNAddressExt".
This field contains the IPv4 address for the PDN connection (PDP context, IP-CAN bearer) when dual-stack IPv4 IPv6 is used, and the IPv6 prefix is included in Served PDP Address or Served PDP/PDN Address.

#### 2.4.81 Service Centre Address
The field is correspond to the ASN.1 field "serviceCentre".
This field contains an ITU-T E.164 number identifying a particular service centre e.g. Short Message Service (SMS) centre (see 3GPP TS 23.040 ).

#### 2.4.82 Serving Node Address
The field is correspond to the ASN.1 field "servingNodeAddress".
These fields contain one or several control plane IP addresses of SGSN, MME, ePDG, HSGW or S-GW, which have been connected during the record. If both an IPv4 and an IPv6 address of the SGSN/S-GW/MME/ePDG/HSGW are available, the S-GW/P-GW shall include the IPv4 address in the CDR.

#### 2.4.83 Serving node IPv6 Address
The field is correspond to the ASN.1 field "servingNodeAddressIPv6".
This field contains the IPv6 address for the Serving node address when dual-stack IPv4 IPv6 is used.

#### 2.4.84 Serving Node PLMN Identifier
The field is correspond to the ASN.1 field "servingNodePLMNIdentifier".
This field contains a serving node (SGSN/S-GW/MME/ePDG/HSGW) PLMN Identifier (Mobile Country Code and Mobile Network Code).

#### 2.4.85 Serving Node Type
The field is correspond to the ASN.1 field "servingNodeType".
These fields contain one or several serving node types in control plane of S-GW or P-GW, which have been connected during the record. The serving node types listed here map to the serving node addresses listed in the field "Serving node Address" in sequence.

#### 2.4.86 SGSN Address
The field is correspond to the ASN.1 field "sgsnAddress".
These fields contain one or several IP addresses of SGSN. The G-CDR fields contain the address of the current GGSN and a list of SGSNs addresses, which have been connected during the record (SGSN change due to inter SGSN Routing Area update). The M-CDR fields only contain the address of the current SGSN. It does not provide any information related to active PDP context(s) and thus the connected (used) GGSN(s) cannot be identified.

#### 2.4.87 SGSN Change
The field is correspond to the ASN.1 field "sgsnChange".
This field is present only in the S-CDR to indicate that this is the first record after SGSN being switched.

#### 2.4.88 SGSN PLMN Identifier
The field is correspond to the ASN.1 field "sgsnPLMNIdentifier".
This field contains an SGSN PLMN Identifier (Mobile Country Code and Mobile Network Code).This implies that when the UE moves to another PLMN, the G-CDR has to be closed.

#### 2.4.89 S-GW Address Used
The field is correspond to the ASN.1 field "s-GWAddress".
These field is the serving S-GW IP Address for the Control Plane. If both an IPv4 and an IPv6 address of the S-GW is available, the S-GW shall include the IPv4 address in the CDR.

#### 2.4.90 S-GW Address IPv6
The field is correspond to the ASN.1 field "s-GWAddressUsedIPv6".
This field contains the IPv6 address for the S-GW address when dual-stack IPv4 IPv6 is used.

#### 2.4.91 SGW Change
The field is correspond to the ASN.1 field "sGWChange".
This field is present only in the SGW-CDR to indicate that this is the first record after an S-GW/ePDG change.

#### 2.4.92 Short Message Service (SMS) Result
The field is correspond to the ASN.1 field "smsResult".
This field contains the result of delivering a short message to a service centre or a mobile subscriber. (See 3GPP TS 29.002). This field is only provided if the attempted delivery was unsuccessful.

#### 2.4.93 Start Time
The field is correspond to the ASN.1 field "startTime".
This field contains the start time in the S-GW/P-GW for a IP-CAN bearer. This field is only provided in the first CDR of IP-CAN bearer.

#### 2.4.94 Stop Time
The field is correspond to the ASN.1 field "stopTime".
This field contains the stop time in the S-GW/P-GW for a IP-CAN bearer. This field is only provided in the last CDR of IP-CAN bearer.

#### 2.4.95 System Type
The field is correspond to the ASN.1 field "systemType".
This field is present conditionally, indicating the use of the 3G air-interface or GERAN interface. In the case of service provided by a GSM air interface, this field is not present.

#### 2.4.96 TWAN User Location Information
The field is correspond to the ASN.1 field "tWANUserLocationInformation".
This field holds the UE location in a Trusted WLAN Access Network (TWAN), i.e BSSID and SSID of the access point.

#### 2.4.97 User CSG Information
The field is correspond to the ASN.1 field "userCSGInformation".
This field contains the "User CSG Information" status of the user accessing a CSG cell: it comprises CSG ID within the PLMN, Access mode and indication on CSG membership for the user when hybrid access applies.

#### 2.4.98 User Location Information
The field is correspond to the ASN.1 field "userLocationInformation/ePCUserLocationInformation".
This field contains the User Location Information as described in:
- TS 29.060 [75] for GTP case (e.g. CGI, SAI, RAI)
- TS 29.274 [91] for eGTP case (e.g. CGI, SAI, RAI TAI and ECGI)
- TS 29.275 [92] for PMIP case

The field is provided by the SGSN/MME and transferred to the S-GW/P-GW during the IP-CAN bearer activation/modification.

#### 2.4.99 UWAN User Location Information
The field is correspond to the ASN.1 field "uWANUserLocationInformation".
This field contains the UE location in an Untrusted Wireless Access Network (UWAN) which includes the UE local IP address and optionally UDP source port number (if NAT is detected) as defined in TS 29.274 [210]. It may also include WLAN location information (SSID and, when available, BSSID of the access point) the ePDG may have received from the 3GPP AAA server about the UE as defined in TS 29.274 [210].

---

## 3. ASN.1 Definitions for CDR

### 3.1 CDR Structure

#### 3.1.1 CallEventRecord

```asn1
CallEventRecord ::= CHOICE
{
    sgsnPDPRecord       [20] SGSNPDPRecord,
    ggsnPDPRecord       [21] GGSNPDPRecord,
    sgsnMMRecord        [22] SGSNMMRecord,
    sgsnSMORecord       [23] SGSNSMORecord,
    sgsnSMTRecord       [24] SGSNSMTRecord,
    sgsnMtLCSRecord     [25] SGSNMTLCSRecord,
    sgsnMoLCSRecord     [26] SGSNMOLCSRecord,
    sgsnNiLCSRecord     [27] SGSNNILCSRecord,
    sgsnMBMSRecord      [29] SGSNMBMSRecord,
    ggsnMBMSRecord      [30] GGSNMBMSRecord,
    sGWRecord           [78] SGWRecord,
    pGWRecord           [79] PGWRecord,
    wLANRecord          [80] WLANRecord,
    gwMBMSRecord        [86] GWMBMSRecord,
    ePDGRecord          [96] EPDGRecord,
    hSGWRecord          [200] HSGWRecord
}
```

#### 3.1.2 SGSNPDPRecord

```asn1
SGSNPDPRecord ::= SET
{
    recordType                      [0]  CallEventRecordType,
    networkInitiation               [1]  NetworkInitiatedPDPContext OPTIONAL,
    servedIMSI                      [3]  IMSI OPTIONAL,
    servedIMEI                      [4]  IMEI OPTIONAL,
    sgsnAddressList                 [5]  SEQUENCE OF GSNAddress OPTIONAL,
    msNetworkCapability             [6]  MSNetworkCapability OPTIONAL,
    routingArea                     [7]  RoutingAreaCode OPTIONAL,
    locationAreaCode                [8]  LocationAreaCode OPTIONAL,
    cellIdentifier                  [9]  CellID OPTIONAL,
    chargingID                      [10] ChargingID OPTIONAL,
    ggsnAddressUsed                 [11] GSNAddress OPTIONAL,
    accessPointNameNI               [12] AccessPointNameNI OPTIONAL,
    pdpType                         [13] PDPType OPTIONAL,
    servedPDPAddress                [14] PDPAddress OPTIONAL,
    listOfTrafficVolumes            [15] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordOpeningTime               [16] TimeStamp OPTIONAL,
    duration                        [17] CallDuration OPTIONAL,
    sgsnChange                      [18] SGSNChange OPTIONAL,
    causeForRecClosing              [19] CauseForRecClosing OPTIONAL,
    diagnostics                     [20] Diagnostics OPTIONAL,
    listOfRecordSequenceNumber      [21] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    nodeID                          [22] NodeID OPTIONAL,
    recordExtensions                [23] ManagementExtensions OPTIONAL,
    localSequenceNumber             [24] RecordSeqNumber,
    apnSelectionMode                [25] APNSelectionMode OPTIONAL,
    accessPointNameOI               [26] AccessPointNameOI OPTIONAL,
    servedMSISDN                    [27] MSISDN OPTIONAL,
    chargingCharacteristics         [28] ChargingCharacteristics OPTIONAL,
    systemType                      [29] SystemType OPTIONAL,
    cAMELInformationPDP             [30] CAMELInformationPDP OPTIONAL,
    rNCUnsentDownlinkVolume         [31] DataVolumeGPRS OPTIONAL,
    consolidationResult             [32] ConsolidationResult OPTIONAL,
    chgLocalSeqNoList               [33] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    chChSelectionMode               [34] ChChSelectionMode OPTIONAL,
    dynamicAddressFlag              [35] DynamicAddressFlag OPTIONAL,
    cellPLMNID                      [36] PlmnId OPTIONAL,
    servedPDPPDNAddressExt          [37] PDPAddress OPTIONAL
}
```

#### 3.1.3 GGSNPDPRecord

```asn1
GGSNPDPRecord ::= SET
{
    recordType                      [0]  CallEventRecordType,
    networkInitiation               [1]  NetworkInitiatedPDPContext OPTIONAL,
    servedIMSI                      [3]  IMSI OPTIONAL,
    ggsnAddress                     [4]  GSNAddress OPTIONAL,
    chargingID                      [5]  ChargingID OPTIONAL,
    sgsnAddress                     [6]  SEQUENCE OF GSNAddress OPTIONAL,
    accessPointNameNI               [7]  AccessPointNameNI OPTIONAL,
    pdpType                         [8]  PDPType OPTIONAL,
    servedPDPAddress                [9]  PDPAddress OPTIONAL,
    dynamicAddressFlag              [11] DynamicAddressFlag OPTIONAL,
    listOfTrafficVolumes            [12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordOpeningTime               [13] TimeStamp OPTIONAL,
    duration                        [14] CallDuration OPTIONAL,
    causeForRecClosing              [15] CauseForRecClosing OPTIONAL,
    diagnostics                     [16] Diagnostics OPTIONAL,
    listOfRecordSequenceNumber      [17] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    nodeID                          [18] NodeID OPTIONAL,
    recordExtensions                [19] ContentExtensions OPTIONAL,
    localSequenceNumber             [20] RecordSeqNumber,
    apnSelectionMode                [21] APNSelectionMode OPTIONAL,
    servedMSISDN                    [22] MSISDN OPTIONAL,
    chargingCharacteristics         [23] ChargingCharacteristics OPTIONAL,
    localSeqNoList                  [26] AddressSequenceNumberList OPTIONAL,
    sgsnPLMNIdentifier              [27] PlmnId OPTIONAL,
    chChSelectionMode               [28] ChChSelectionMode OPTIONAL,
    rATType                         [29] RATType OPTIONAL,
    consolidationResult             [30] ConsolidationResult OPTIONAL,
    iMSsignalingContext             [32] NULL OPTIONAL,
    externalChargingID              [33] OCTET STRING OPTIONAL,
    userLocationInformation         [34] UmtsUserLocationInformation OPTIONAL,
    servedIMEISV                    [35] IMEI OPTIONAL,
    mSTimeZone                      [36] MSTimeZone OPTIONAL,
    url                             [37] IA5String (SIZE(1..64)) OPTIONAL,
    iMSIunauthenticatedFlag         [38] NULL OPTIONAL,
    roamingIndicator                [101] INTEGER OPTIONAL,
    diameterSessionID               [102] IA5String (SIZE(1..128)) OPTIONAL,
    servedPDPPDNAddressExt          [103] PDPAddress OPTIONAL,
    pSFurnishChargingInformation    [104] PSFurnishChargingInformation OPTIONAL,
    ePCUserLocationInformation      [105] UserLocationInformation OPTIONAL,
    ePCQoSInformation               [106] EPCQoSInformation OPTIONAL
}
```


### 3.1.4 SGSNMMRecord

```asn1
SGSNMMRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [1] IMSI OPTIONAL,
    servedIMEI [2] IMEI OPTIONAL,
    sgsnAddress [3] GSNAddress OPTIONAL,
    msNetworkCapability [4] MSNetworkCapability OPTIONAL,
    routingArea [5] RoutingAreaCode OPTIONAL,
    locationAreaCode [6] LocationAreaCode OPTIONAL,
    cellIdentifier [7] CellID OPTIONAL,
    changeLocation [8] SEQUENCE OF ChangeLocation OPTIONAL,
    recordOpeningTime [9] TimeStamp OPTIONAL,
    duration [10] CallDuration OPTIONAL,
    sgsnChange [11] SGSNChange OPTIONAL,
    causeForRecClosing [12] CauseForRecClosing OPTIONAL,
    diagnostics [13] Diagnostics OPTIONAL,
    recordSequenceNumber [14] INTEGER OPTIONAL,
    nodeID [15] NodeID OPTIONAL,
    recordExtensions [16] ManagementExtensions OPTIONAL,
    localSequenceNumber [17] RecordSeqNumber,
    servedMSISDN [18] MSISDN OPTIONAL,
    chargingCharacteristics [19] ChargingCharacteristics OPTIONAL,
    cAMELInformationMM [20] CAMELInformationMM OPTIONAL,
    systemType [21] SystemType OPTIONAL,
    chChSelectionMode [22] ChChSelectionMode OPTIONAL,
    cellPLMNID [23] PlmnId OPTIONAL
}
```

### 3.1.5 SGSNSMORecord

```asn1
SGSNSMORecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [1] IMSI OPTIONAL,
    servedIMEI [2] IMEI OPTIONAL,
    servedMSISDN [3] MSISDN OPTIONAL,
    msNetworkCapability [4] MSNetworkCapability OPTIONAL,
    serviceCentre [5] AddressString OPTIONAL,
    recordingEntity [6] RecordingEntity OPTIONAL,
    locationArea [7] LocationAreaCode OPTIONAL,
    routingArea [8] RoutingAreaCode OPTIONAL,
    cellIdentifier [9] CellID OPTIONAL,
    messageReference [10] MessageReference OPTIONAL,
    originationTime [11] TimeStamp OPTIONAL,
    smsResult [12] SMSResult OPTIONAL,
    recordExtensions [13] ManagementExtensions OPTIONAL,
    nodeID [14] NodeID OPTIONAL,
    localSequenceNumber [15] RecordSeqNumber,
    chargingCharacteristics [16] ChargingCharacteristics OPTIONAL,
    systemType [17] SystemType OPTIONAL,
    destinationNumber [18] BCDDirectoryNumber OPTIONAL,
    cAMELInformationSMS [19] CAMELInformationSMS OPTIONAL,
    chChSelectionMode [20] ChChSelectionMode OPTIONAL,
    cellPLMNID [101] PlmnId OPTIONAL
}
```

### 3.1.6 SGSNSMTRecord

```asn1
SGSNSMTRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [1] IMSI OPTIONAL,
    servedIMEI [2] IMEI OPTIONAL,
    servedMSISDN [3] MSISDN OPTIONAL,
    msNetworkCapability [4] MSNetworkCapability OPTIONAL,
    serviceCentre [5] AddressString OPTIONAL,
    recordingEntity [6] RecordingEntity OPTIONAL,
    locationArea [7] LocationAreaCode OPTIONAL,
    routingArea [8] RoutingAreaCode OPTIONAL,
    cellIdentifier [9] CellID OPTIONAL,
    originationTime [10] TimeStamp OPTIONAL,
    smsResult [11] SMSResult OPTIONAL,
    recordExtensions [12] ManagementExtensions OPTIONAL,
    nodeID [13] NodeID OPTIONAL,
    localSequenceNumber [14] RecordSeqNumber,
    chargingCharacteristics [15] ChargingCharacteristics OPTIONAL,
    systemType [16] SystemType OPTIONAL,
    cAMELInformationSMS [17] CAMELInformationSMS OPTIONAL,
    chChSelectionMode [18] ChChSelectionMode OPTIONAL,
    originationNumber [19] BCDDirectoryNumber OPTIONAL,
    cellPLMNID [101] PlmnId OPTIONAL
}
```

### 3.1.7 SGSNMOLCSRecord

```asn1
SGSNMOLCSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    recordingEntity [1] RecordingEntity OPTIONAL,
    lcsClientType [2] LCSClientType OPTIONAL,
    lcsClientIdentity [3] LCSClientIdentity OPTIONAL,
    servedIMSI [4] IMSI OPTIONAL,
    servedMSISDN [5] MSISDN OPTIONAL,
    sgsnAddress [6] GSNAddress OPTIONAL,
    locationMethod [7] LocationMethod OPTIONAL,
    lcsQos [8] LCSQoSInfo OPTIONAL,
    lcsPriority [9] LCS-Priority OPTIONAL,
    mlcNumber [10] ISDN-AddressString OPTIONAL,
    eventTimeStamp [11] TimeStamp OPTIONAL,
    measurementDuration [12] CallDuration OPTIONAL,
    location [13] LocationAreaAndCell OPTIONAL,
    routingArea [14] RoutingAreaCode OPTIONAL,
    locationEstimate [15] Ext-GeographicalInformation OPTIONAL,
    positioningData [16] PositioningData OPTIONAL,
    lcsCause [17] LCSCause OPTIONAL,
    diagnostics [18] Diagnostics OPTIONAL,
    nodeID [19] NodeID OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    chargingCharacteristics [21] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [22] ChChSelectionMode OPTIONAL,
    rATType [23] RATType OPTIONAL,
    recordExtensions [24] ManagementExtensions OPTIONAL,
    causeForRecClosing [25] CauseForRecClosing OPTIONAL
}
```

### 3.1.8 SGSNMTLCSRecord

```asn1
SGSNMTLCSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    recordingEntity [1] RecordingEntity OPTIONAL,
    lcsClientType [2] LCSClientType OPTIONAL,
    lcsClientIdentity [3] LCSClientIdentity OPTIONAL,
    servedIMSI [4] IMSI OPTIONAL,
    servedMSISDN [5] MSISDN OPTIONAL,
    sgsnAddress [6] GSNAddress OPTIONAL,
    locationType [7] LocationType OPTIONAL,
    lcsQos [8] LCSQoSInfo OPTIONAL,
    lcsPriority [9] LCS-Priority OPTIONAL,
    mlcNumber [10] ISDN-AddressString OPTIONAL,
    eventTimeStamp [11] TimeStamp OPTIONAL,
    measurementDuration [12] CallDuration OPTIONAL,
    notificationToMSUser [13] NotificationToMSUser OPTIONAL,
    privacyOverride [14] NULL OPTIONAL,
    location [15] LocationAreaAndCell OPTIONAL,
    routingArea [16] RoutingAreaCode OPTIONAL,
    locationEstimate [17] Ext-GeographicalInformation OPTIONAL,
    positioningData [18] PositioningData OPTIONAL,
    lcsCause [19] LCSCause OPTIONAL,
    diagnostics [20] Diagnostics OPTIONAL,
    nodeID [21] NodeID OPTIONAL,
    localSequenceNumber [22] RecordSeqNumber,
    chargingCharacteristics [23] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [24] ChChSelectionMode OPTIONAL,
    rATType [25] RATType OPTIONAL,
    recordExtensions [26] ManagementExtensions OPTIONAL,
    causeForRecClosing [27] CauseForRecClosing OPTIONAL
}
```

### 3.1.9 SGSNNILCSRecord

```asn1
SGSNNILCSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    recordingEntity [1] RecordingEntity OPTIONAL,
    lcsClientType [2] LCSClientType OPTIONAL,
    lcsClientIdentity [3] LCSClientIdentity OPTIONAL,
    servedIMSI [4] IMSI OPTIONAL,
    servedMSISDN [5] MSISDN OPTIONAL,
    sgsnAddress [6] GSNAddress OPTIONAL,
    servedIMEI [7] IMEI OPTIONAL,
    lcsQos [8] LCSQoSInfo OPTIONAL,
    lcsPriority [9] LCS-Priority OPTIONAL,
    mlcNumber [10] ISDN-AddressString OPTIONAL,
    eventTimeStamp [11] TimeStamp OPTIONAL,
    measurementDuration [12] CallDuration OPTIONAL,
    location [13] LocationAreaAndCell OPTIONAL,
    routingArea [14] RoutingAreaCode OPTIONAL,
    locationEstimate [15] Ext-GeographicalInformation OPTIONAL,
    positioningData [16] PositioningData OPTIONAL,
    lcsCause [17] LCSCause OPTIONAL,
    diagnostics [18] Diagnostics OPTIONAL,
    nodeID [19] NodeID OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    chargingCharacteristics [21] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [22] ChChSelectionMode OPTIONAL,
    rATType [23] RATType OPTIONAL,
    recordExtensions [24] ManagementExtensions OPTIONAL,
    causeForRecClosing [25] CauseForRecClosing OPTIONAL
}
```

### 3.1.10 SGSNMBMSRecord

```asn1
SGSNMBMSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    ggsnAddress [1] GSNAddress OPTIONAL,
    chargingID [2] ChargingID OPTIONAL,
    listofRAs [3] SEQUENCE OF RAIdentity OPTIONAL,
    accessPointNameNI [4] AccessPointNameNI OPTIONAL,
    servedPDPAddress [5] PDPAddress OPTIONAL,
    listOfTrafficVolumes [6] SEQUENCE OF ChangeOfMBMSCondition OPTIONAL,
    recordOpeningTime [7] TimeStamp OPTIONAL,
    duration [8] CallDuration OPTIONAL,
    causeForRecClosing [9] CauseForRecClosing OPTIONAL,
    diagnostics [10] Diagnostics OPTIONAL,
    recordSequenceNumber [11] INTEGER OPTIONAL,
    nodeID [12] NodeID OPTIONAL,
    recordExtensions [13] ManagementExtensions OPTIONAL,
    localSequenceNumber [14] RecordSeqNumber,
    sgsnPLMNIdentifier [15] PlmnId OPTIONAL,
    numberofReceivingUE [16] INTEGER OPTIONAL,
    mbmsInformation [17] MBMSInformation OPTIONAL
}
```

### 3.1.11 GGSNMBMSRecord

```asn1
GGSNMBMSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    ggsnAddress [1] GSNAddress OPTIONAL,
    chargingID [2] ChargingID OPTIONAL,
    listofDownstreamNodes [3] SEQUENCE OF GSNAddress OPTIONAL,
    accessPointNameNI [4] AccessPointNameNI OPTIONAL,
    servedPDPAddress [5] PDPAddress OPTIONAL,
    listOfTrafficVolumes [6] SEQUENCE OF ChangeOfMBMSCondition OPTIONAL,
    recordOpeningTime [7] TimeStamp OPTIONAL,
    duration [8] CallDuration OPTIONAL,
    causeForRecClosing [9] CauseForRecClosing OPTIONAL,
    diagnostics [10] Diagnostics OPTIONAL,
    recordSequenceNumber [11] INTEGER OPTIONAL,
    nodeID [12] NodeID OPTIONAL,
    recordExtensions [13] ManagementExtensions OPTIONAL,
    localSequenceNumber [14] RecordSeqNumber,
    mbmsInformation [15] MBMSInformation OPTIONAL
}
```

### 3.1.12 GWMBMSRecord

```asn1
GWMBMSRecord ::= SET
{
    recordType [0] CallEventRecordType,
    mbmsGWAddress [1] GSNAddress OPTIONAL,
    chargingID [2] ChargingID OPTIONAL,
    listofDownstreamNodes [3] SEQUENCE OF GSNAddress OPTIONAL,
    accessPointNameNI [4] AccessPointNameNI OPTIONAL,
    pdpPDNType [5] PDPType OPTIONAL,
    servedPDPPDNAddress [6] PDPAddress OPTIONAL,
    listOfTrafficVolumes [7] SEQUENCE OF ChangeOfMBMSCondition OPTIONAL,
    recordOpeningTime [8] TimeStamp OPTIONAL,
    duration [9] CallDuration OPTIONAL,
    causeForRecClosing [10] CauseForRecClosing OPTIONAL,
    diagnostics [11] Diagnostics OPTIONAL,
    recordSequenceNumber [12] INTEGER OPTIONAL,
    nodeID [13] NodeID OPTIONAL,
    recordExtensions [14] ManagementExtensions OPTIONAL,
    localSequenceNumber [15] RecordSeqNumber,
    mbmsInformation [16] MBMSInformation OPTIONAL,
    commonTeid [17] CTEID OPTIONAL,
    iPMulticastSourceAddress [18] PDPAddress OPTIONAL
}
```

### 3.1.13 SGWRecord

```asn1
SGWRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [3] IMSI OPTIONAL,
    s-GWAddress [4] SEQUENCE OF GSNAddress OPTIONAL,
    chargingID [5] ChargingID OPTIONAL,
    servingNodeAddress [6] SEQUENCE OF GSNAddress OPTIONAL,
    accessPointNameNI [7] AccessPointNameNI OPTIONAL,
    pdpPDNType [8] PDPType OPTIONAL,
    servedPDPPDNAddress [9] PDPAddress OPTIONAL,
    dynamicAddressFlag [11] DynamicAddressFlag OPTIONAL,
    listOfTrafficVolumes [12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordOpeningTime [13] TimeStamp OPTIONAL,
    duration [14] CallDuration OPTIONAL,
    causeForRecClosing [15] CauseForRecClosing OPTIONAL,
    diagnostics [16] Diagnostics OPTIONAL,
    listOfRecordSequenceNumber [17] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    nodeID [18] NodeID OPTIONAL,
    recordExtensions [19] ManagementExtensions OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    apnSelectionMode [21] APNSelectionMode OPTIONAL,
    servedMSISDN [22] MSISDN OPTIONAL,
    chargingCharacteristics [23] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [24] ChChSelectionMode OPTIONAL,
    iMSsignalingContext [25] NULL OPTIONAL,
    servingNodePLMNIdentifier [27] PlmnId OPTIONAL,
    servedIMEISV [29] IMEI OPTIONAL,
    rATType [30] RATType OPTIONAL,
    mSTimeZone [31] MSTimeZone OPTIONAL,
    userLocationInformation [32] UserLocationInformation OPTIONAL,
    sGWChange [34] SGWChange OPTIONAL,
    servingNodeType [35] SEQUENCE OF ServingNodeType OPTIONAL,
    p-GWAddressUsed [36] GSNAddress OPTIONAL,
    p-GWPLMNIdentifier [37] PlmnId OPTIONAL,
    startTime [38] TimeStamp OPTIONAL,
    stopTime [39] TimeStamp OPTIONAL,
    pDNConnectionID [40] ChargingID OPTIONAL,
    chgLocalSeqNoList [41] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    consolidationResult [42] ConsolidationResult OPTIONAL,
    iMSIunauthenticatedFlag [43] NULL OPTIONAL,
    lowPriorityIndicator [44] NULL OPTIONAL,
    dynamicAddressFlagExt [47] DynamicAddressFlag OPTIONAL,
    cPCIoTEPSOptimisationIndicator [59] CPCIoTEPSOptimisationIndicator OPTIONAL,
    uNIPDUCPOnlyFlag [60] UNIPDUCPOnlyFlag OPTIONAL,
    servingPLMNRateControl [61] ServingPLMNRateControl OPTIONAL,
    pDPPDNTypeExtension [62] PDPPDNTypeExtension OPTIONAL,
    mOExceptionDataCounter [63] MOExceptionDataCounter OPTIONAL,
    listOfRANSecondaryRATUsageReports [64] SEQUENCE OF RANSecondaryRATUsageReport OPTIONAL,
    servedPDPPDNAddressExt [100] PDPAddress OPTIONAL,
    userCSGInformation [101] UserCSGInformation OPTIONAL
}
```

### 3.1.14 PGWRecord

```asn1
PGWRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [3] IMSI OPTIONAL,
    p-GWAddress [4] GSNAddress OPTIONAL,
    chargingID [5] ChargingID OPTIONAL,
    servingNodeAddress [6] SEQUENCE OF GSNAddress OPTIONAL,
    accessPointNameNI [7] AccessPointNameNI OPTIONAL,
    pdpPDNType [8] PDPType OPTIONAL,
    servedPDPPDNAddress [9] PDPAddress OPTIONAL,
    dynamicAddressFlag [11] DynamicAddressFlag OPTIONAL,
    listOfTrafficVolumes [12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordOpeningTime [13] TimeStamp OPTIONAL,
    duration [14] CallDuration OPTIONAL,
    causeForRecClosing [15] CauseForRecClosing OPTIONAL,
    diagnostics [16] Diagnostics OPTIONAL,
    listOfRecordSequenceNumber [17] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    nodeID [18] NodeID OPTIONAL,
    recordExtensions [19] ManagementExtensions OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    apnSelectionMode [21] APNSelectionMode OPTIONAL,
    servedMSISDN [22] MSISDN OPTIONAL,
    chargingCharacteristics [23] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [24] ChChSelectionMode OPTIONAL,
    iMSsignalingContext [25] NULL OPTIONAL,
    externalChargingID [26] OCTET STRING OPTIONAL,
    servingNodePLMNIdentifier [27] PlmnId OPTIONAL,
    servedIMEISV [29] IMEI OPTIONAL,
    rATType [30] RATType OPTIONAL,
    mSTimeZone [31] MSTimeZone OPTIONAL,
    userLocationInformation [32] UserLocationInformation OPTIONAL,
    listOfServiceData [34] SEQUENCE OF ChangeOfServiceCondition OPTIONAL,
    servingNodeType [35] SEQUENCE OF ServingNodeType OPTIONAL,
    servedMNNAI [36] SubscriptionID OPTIONAL,
    p-GWPLMNIdentifier [37] PlmnId OPTIONAL,
    startTime [38] TimeStamp OPTIONAL,
    stopTime [39] TimeStamp OPTIONAL,
    pDNConnectionID [41] ChargingID OPTIONAL,
    chgLocalSeqNoList [42] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    consolidationResult [43] ConsolidationResult OPTIONAL,
    iMSIunauthenticatedFlag [44] NULL OPTIONAL,
    threeGPP2UserLocationInformation [45] ThreeGPP2UserLocationInformation OPTIONAL,
    tWANUserLocationInformation [46] TWANUserLocationInfo OPTIONAL,
    dynamicAddressFlagExt [47] DynamicAddressFlag OPTIONAL,
    ePCQoSInformation [55] EPCQoSInformation OPTIONAL,
    uWANUserLocationInformation [62] UWANUserLocationInfo OPTIONAL,
    sGiPtPTunnellingMethod [64] SGiPtPTunnellingMethod OPTIONAL,
    uNIPDUCPOnlyFlag [65] UNIPDUCPOnlyFlag OPTIONAL,
    servingPLMNRateControl [66] ServingPLMNRateControl OPTIONAL,
    aPNRateControl [67] APNRateControl OPTIONAL,
    pDPPDNTypeExtension [68] PDPPDNTypeExtension OPTIONAL,
    mOExceptionDataCounter [69] MOExceptionDataCounter OPTIONAL,
    sCSASAddress [72] SCSASAddress OPTIONAL,
    listOfRANSecondaryRATUsageReports [73] SEQUENCE OF RANSecondaryRATUsageReport OPTIONAL,
    roamingIndicator [101] INTEGER OPTIONAL,
    diameterSessionID [102] IA5String (SIZE(1..128)) OPTIONAL,
    servedPDPPDNAddressExt [103] PDPAddress OPTIONAL,
    userCSGInformation [105] UserCSGInformation OPTIONAL,
    pSFurnishChargingInformation [106] PSFurnishChargingInformation OPTIONAL,
    lowPriorityIndicator [107] NULL OPTIONAL
}
```

### 3.1.15 WLANRecord

```asn1
WLANRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [1] IMSI OPTIONAL,
    servedMSISDN [2] MSISDN OPTIONAL,
    servedIMEI [3] IMEI OPTIONAL,
    pDGAddress [4] GSNAddress OPTIONAL,
    nodeID [5] NodeID OPTIONAL,
    servingProxyAddress [6] GSNAddress OPTIONAL,
    pdpType [7] PDPType OPTIONAL,
    remoteIPAddress [8] PDPAddress OPTIONAL,
    chargingID [9] ChargingID OPTIONAL,
    wLanSessionID [10] INTEGER (0..4294967295) OPTIONAL,
    accessPointNameNI [11] AccessPointNameNI OPTIONAL,
    chargingCharacteristics [12] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [13] ChChSelectionMode OPTIONAL,
    recordOpeningTime [14] TimeStamp OPTIONAL,
    duration [15] CallDuration OPTIONAL,
    causeForRecClosing [16] CauseForRecClosing OPTIONAL,
    listOfRecordSequenceNumber [17] SEQUENCE OF AddressSequenceNumberList OPTIONAL,
    localSequenceNumber [18] RecordSeqNumber,
    diagnostics [19] Diagnostics OPTIONAL,
    listOfTrafficVolumes [20] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordExtensions [21] ContentExtensions OPTIONAL
}
```

### 3.1.16 HSGWRecord

```asn1
HSGWRecord ::= SET
{
    recordType [0] CallEventRecordType,
    servedIMSI [3] IMSI OPTIONAL,
    s-GWAddressUsed [4] IPAddress OPTIONAL,
    chargingID [5] ChargingID OPTIONAL,
    servingNodeAddress [6] SEQUENCE OF IPAddress OPTIONAL,
    accessPointNameNI [7] AccessPointNameNI OPTIONAL,
    pdpPDNType [8] PDPType OPTIONAL,
    servedPDPPDNAddress [9] PDPAddress OPTIONAL,
    dynamicAddressFlag [11] DynamicAddressFlag OPTIONAL,
    listOfTrafficVolumes [12] SEQUENCE OF CTC-ChangeOfCharCondition OPTIONAL,
    recordOpeningTime [13] TimeStamp OPTIONAL,
    duration [14] CallDuration OPTIONAL,
    causeForRecClosing [15] CauseForRecClosing OPTIONAL,
    diagnostics [16] Diagnostics OPTIONAL,
    recordSequenceNumber [17] INTEGER OPTIONAL,
    nodeID [18] NodeID OPTIONAL,
    recordExtensions [19] ManagementExtensions OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    apnSelectionMode [21] APNSelectionMode OPTIONAL,
    servedMSISDN [22] MSISDN OPTIONAL,
    chargingCharacteristics [23] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [24] ChChSelectionMode OPTIONAL,
    servingNodePLMNIdentifier [27] PlmnId OPTIONAL,
    servedIMEISV [29] IMEI OPTIONAL,
    rATType [30] RATType OPTIONAL,
    threeGPP2UserLocationInformation [32] ThreeGPP2UserLocationInformation OPTIONAL,
    sGWChange [34] SGWChange OPTIONAL,
    servingNodeType [35] SEQUENCE OF ServingNodeType OPTIONAL,
    p-GWAddressUsed [36] IPAddress OPTIONAL,
    startTime [38] TimeStamp OPTIONAL,
    stopTime [39] TimeStamp OPTIONAL,
    served3gpp2MEID [40] Served3GPP2MEID OPTIONAL,
    iMSIunauthenticatedFlag [41] NULL OPTIONAL,
    servedPDPPDNAddressExt [43] PDPAddress OPTIONAL,
    dynamicAddressFlagExt [47] DynamicAddressFlag OPTIONAL,
    s-GWAddressUsedIPv6 [48] IPAddress OPTIONAL,
    servingNodeAddressIPv6 [49] SEQUENCE OF IPAddress OPTIONAL,
    p-GWAddressUsedIPv6 [50] IPAddress OPTIONAL
}
```

### 3.1.17 EPDGRecord

```asn1
EPDGRecord ::= SET
{
    recordType [0] RecordType,
    servedIMSI [3] IMSI OPTIONAL,
    ePDGAddressUsed [4] GSNAddress OPTIONAL,
    chargingID [5] ChargingID OPTIONAL,
    accessPointNameNI [7] AccessPointNameNI OPTIONAL,
    pdpPDNType [8] PDPType OPTIONAL,
    servedPDPPDNAddress [9] PDPAddress OPTIONAL,
    dynamicAddressFlag [11] DynamicAddressFlag OPTIONAL,
    listOfTrafficVolumes [12] SEQUENCE OF ChangeOfCharCondition OPTIONAL,
    recordOpeningTime [13] TimeStamp OPTIONAL,
    duration [14] CallDuration OPTIONAL,
    causeForRecClosing [15] CauseForRecClosing OPTIONAL,
    diagnostics [16] Diagnostics OPTIONAL,
    recordSequenceNumber [17] INTEGER OPTIONAL,
    nodeID [18] NodeID OPTIONAL,
    recordExtensions [19] ManagementExtensions OPTIONAL,
    localSequenceNumber [20] RecordSeqNumber,
    apnSelectionMode [21] APNSelectionMode OPTIONAL,
    servedMSISDN [22] MSISDN OPTIONAL,
    chargingCharacteristics [23] ChargingCharacteristics OPTIONAL,
    chChSelectionMode [24] ChChSelectionMode OPTIONAL,
    iMSsignalingContext [25] NULL OPTIONAL,
    rATType [30] RATType OPTIONAL,
    sGWChange [34] SGWChange OPTIONAL,
    p-GWAddressUsed [36] GSNAddress OPTIONAL,
    p-GWPLMNIdentifier [37] PlmnId OPTIONAL,
    startTime [38] TimeStamp OPTIONAL,
    stopTime [39] TimeStamp OPTIONAL,
    pDNConnectionChargingID [40] ChargingID OPTIONAL,
    servedPDPPDNAddressExt [43] PDPAddress OPTIONAL,
    dynamicAddressFlagExt [47] DynamicAddressFlag OPTIONAL,
    uWANUserLocationInformation [53] UWANUserLocationInfo OPTIONAL
}
```

## 3.2 CDR Fields Structure

### 3.2.1 AccessPointNameNI

```asn1
AccessPointNameNI ::= IA5String (SIZE(1..100))
```

### 3.2.2 AccessPointNameOI

```asn1
AccessPointNameOI ::= IA5String (SIZE(1..37))
```

### 3.2.3 AdditionalExceptionReports

```asn1
AdditionalExceptionReports ::= ENUMERATED
{
    notAllowed (0),
    allowed (1)
}
```

### 3.2.4 AddressSequenceNumberList

```asn1
AddressSequenceNumberList ::= SEQUENCE
{
    gsnAddress [0] GSNAddress OPTIONAL,
    sequenceNumberList [1] SEQUENCE OF INTEGER OPTIONAL
}
```

### 3.2.5 AddressString

```asn1
AddressString ::= OCTET STRING (SIZE (1..maxAddressLength))
maxAddressLength INTEGER ::= 20
```

The format includes:
- Octet 1: Extension (bit 8=1), Nature of Address (bits 7-5), Numbering Plan (bits 4-1)
- Octets 2-n: Number digits

Nature of address values:
- 000 = unknown
- 001 = international number
- 010 = national significant number
- 011 = network specific number
- 100 = subscriber number
- 101 = reserved
- 110 = abbreviated number
- 111 = reserved for extension

Numbering plan values:
- 0001 = ISDN/telephony numbering plan
- 0110 = land mobile numbering plan
- 1000 = national numbering plan
- 1001 = private numbering plan

### 3.2.6 AFChargingIdentifier

```asn1
AFChargingIdentifier ::= OCTET STRING (SIZE(1..63))
```

### 3.2.7 AFRecordInformation

```asn1
AFRecordInformation ::= SEQUENCE
{
    aFChargingIdentifier [1] AFChargingIdentifier OPTIONAL
}
```

### 3.2.8 APNRateControl

```asn1
APNRateControl ::= SEQUENCE
{
    aPNRateControlUplink [0] APNRateControlParameters OPTIONAL,
    aPNRateControlDownlink [1] APNRateControlParameters OPTIONAL
}
```

### 3.2.9 APNRateControlParameters

```asn1
APNRateControlParameters ::= SEQUENCE
{
    additionalExceptionReports [0] AdditionalExceptionReports OPTIONAL,
    rateControlTimeUnit [1] RateControlTimeUnit OPTIONAL,
    rateControlMaxRate [2] INTEGER OPTIONAL
}
```

### 3.2.10 APNSelectionMode

```asn1
APNSelectionMode::= ENUMERATED
{
    mSorNetworkProvidedSubscriptionVerified (0),
    mSProvidedSubscriptionNotVerified (1),
    networkProvidedSubscriptionNotVerified (2)
}
```

### 3.2.11 BCDDirectoryNumber

```asn1
BCDDirectoryNumber ::= OCTET STRING
```

Format:
- Octet 1: Extension, Type of Number, Numbering Plan
- Octet 2 (optional): Extension, Presentation Indicator, Screening Indicator
- Octets 3+: Number digits (2 per octet)

Type of Number values:
- 001 = international number
- 010 = national number
- 011 = network specific number

### 3.2.12 BSID

```asn1
BSID ::= OCTET STRING (SIZE(1..12))
```

### 3.2.13 CallDuration

```asn1
CallDuration ::= INTEGER
```

### 3.2.14 CallEventRecordType

```asn1
CallEventRecordType ::= INTEGER
{
    sgsnPDPRecord (18),
    ggsnPDPRecord (19),
    sgsnMMRecord (20),
    sgsnSMORecord (21),
    sgsnSMTRecord (22),
    sgsnMtLCSRecord (26),
    sgsnMoLCSRecord (27),
    sgsnNiLCSRecord (28),
    sgsnMBMSRecord (45),
    ggsnMBMSRecord (46),
    sGWRecord (84),
    pGWRecord (85),
    gwMBMSRecord (86),
    ePDGRecord (96),
    wLANRecord (201),
    hSGWRecord (200)
}
```

### 3.2.15 CallingNumber

```asn1
CallingNumber ::= BCDDirectoryNumber
```

### 3.2.16 CallReferenceNumber

```asn1
CallReferenceNumber ::= OCTET STRING (SIZE (1..8))
```

### 3.2.17 CAMELAccessPointNameNI

```asn1
CAMELAccessPointNameNI ::= AccessPointNameNI
```

### 3.2.18 CAMELAccessPointNameOI

```asn1
CAMELAccessPointNameOI ::= AccessPointNameOI
```

### 3.2.19 CAMELInformationMM

```asn1
CAMELInformationMM ::= SET
{
    sCFAddress [1] SCFAddress OPTIONAL,
    serviceKey [2] ServiceKey OPTIONAL,
    defaultTransactionHandling [3] DefaultGPRS-Handling OPTIONAL,
    numberOfDPEncountered [4] NumberOfDPEncountered OPTIONAL,
    levelOfCAMELService [5] LevelOfCAMELService OPTIONAL,
    freeFormatData [6] FreeFormatData OPTIONAL,
    fFDAppendIndicator [7] FFDAppendIndicator OPTIONAL
}
```

### 3.2.20 CAMELInformationPDP

```asn1
CAMELInformationPDP ::= SET
{
    sCFAddress [1] SCFAddress OPTIONAL,
    serviceKey [2] ServiceKey OPTIONAL,
    defaultTransactionHandling [3] DefaultGPRS-Handling OPTIONAL,
    cAMELAccessPointNameNI [4] CAMELAccessPointNameNI OPTIONAL,
    cAMELAccessPointNameOI [5] CAMELAccessPointNameOI OPTIONAL,
    numberOfDPEncountered [6] NumberOfDPEncountered OPTIONAL,
    levelOfCAMELService [7] LevelOfCAMELService OPTIONAL,
    freeFormatData [8] FreeFormatData OPTIONAL,
    fFDAppendIndicator [9] FFDAppendIndicator OPTIONAL
}
```

### 3.2.21 CAMELInformationSMS

```asn1
CAMELInformationSMS ::= SET
{
    sCFAddress [1] SCFAddress OPTIONAL,
    serviceKey [2] ServiceKey OPTIONAL,
    defaultSMSHandling [3] DefaultSMS-Handling OPTIONAL,
    cAMELCallingPartyNumber [4] CallingNumber OPTIONAL,
    cAMELDestinationSubscriberNumber [5] SmsTpDestinationNumber OPTIONAL,
    cAMELSMSCAddress [6] AddressString OPTIONAL,
    freeFormatData [7] FreeFormatData OPTIONAL,
    sMSReferenceNumber [8] CallReferenceNumber OPTIONAL
}
```

### 3.2.22 CauseForRecClosing

```asn1
CauseForRecClosing ::= INTEGER
{
    normalRelease (0),
    abnormalRelease (4),
    cAMELInitCallRelease (5),
    volumeLimit (16),
    timeLimit (17),
    servingNodeChange (18),
    maxChangeCond (19),
    managementIntervention (20),
    intraSGSNIntersystemChange (21),
    rATChange (22),
    mSTimeZoneChange (23),
    sGSNPLMNIDChange (24),
    mOExceptionDataCounterReceipt (27),
    unauthorizedRequestingNetwork (52),
    unauthorizedLCSClient (53),
    positionMethodFailure (54),
    unknownOrUnreachableLCSClient (58),
    listofDownstreamNodeChange (59),
    pcrfProvideNewQoS (99)
}
```

### 3.2.23 CellID

```asn1
CellID ::= OCTET STRING (SIZE(2))
```

### 3.2.24 ChangeCondition

```asn1
ChangeCondition ::= ENUMERATED
{
    qoSChange (0),
    tariffTime (1),
    recordClosure (2),
    failureHandlingContinueOngoing (3),
    failureHandlingRetryandTerminateOngoing (4),
    failureHandlingTerminateOngoing (5),
    cGI-SAICHange (6),
    rAIChange (7),
    dT-Establishment (8),
    dT-Removal (9),
    eCGIChange (10),
    tAIChange (11),
    userLocationChange (12),
    userPlaneToUEChange (18),
    servingPLMNRateControlChange (19)
}
```

### 3.2.25 ChangeOfCharCondition

```asn1
ChangeOfCharCondition ::= SEQUENCE
{
    -- qosRequested and qosNegotiated used in S-CDR,G-CDR,S-MB-CDR,G-MB-CDR only
    -- ePCUserLocationInformation and ePCQoSInformation used in SGW-CDR and G-CDR only
    -- cPCIoTOptimisationIndicator is used in SGW-CDR only
    qosRequested [1] QoSInformation OPTIONAL,
    qosNegotiated [2] QoSInformation OPTIONAL,
    dataVolumeGPRSUplink [3] DataVolumeGPRS OPTIONAL,
    dataVolumeGPRSDownlink [4] DataVolumeGPRS OPTIONAL,
    changeCondition [5] ChangeCondition OPTIONAL,
    changeTime [6] TimeStamp OPTIONAL,
    tariffLevel [7] INTEGER OPTIONAL,
    ePCUserLocationInformation [8] UserLocationInformation OPTIONAL,
    ePCQoSInformation [9] EPCQoSInformation OPTIONAL,
    uWANUserLocationInformation [17] UWANUserLocationInfo OPTIONAL,
    cPCIoTEPSOptimisationIndicator [19] CPCIoTEPSOptimisationIndicator OPTIONAL,
    servingPLMNRateControl [20] ServingPLMNRateControl OPTIONAL
}
```

### 3.2.26 ChangeOfMBMSCondition

```asn1
ChangeOfMBMSCondition::= SEQUENCE
{
    -- Used in MBMS record
    qosRequested [1] QoSInformation OPTIONAL,
    qosNegotiated [2] QoSInformation OPTIONAL,
    dataVolumeMBMSUplink [3] DataVolumeMBMS OPTIONAL,
    dataVolumeMBMSDownlink [4] DataVolumeMBMS OPTIONAL,
    changeCondition [5] ChangeCondition OPTIONAL,
    changeTime [6] TimeStamp OPTIONAL,
    tariffLevel [7] INTEGER OPTIONAL
}
```

### 3.2.27 ChangeOfServiceCondition

```asn1
ChangeOfServiceCondition::= SEQUENCE
{
    ratingGroup [1] RatingGroupId OPTIONAL,
    chargingRuleBaseName [2] ChargingRuleBaseName OPTIONAL,
    resultCode [3] ResultCode OPTIONAL,
    localSequenceNumber [4] INTEGER OPTIONAL,
    timeOfFirstUsage [5] TimeStamp OPTIONAL,
    timeOfLastUsage [6] TimeStamp OPTIONAL,
    timeUsage [7] CallDuration OPTIONAL,
    serviceConditionChange [8] ServiceConditionChange OPTIONAL,
    qoSInformationNeg [9] QoSInformation OPTIONAL,
    servingNodeAddress [10] GSNAddress OPTIONAL,
    datavolumeFBCUplink [12] INTEGER OPTIONAL,
    datavolumeFBCDownlink [13] INTEGER OPTIONAL,
    timeOfReport [14] TimeStamp OPTIONAL,
    failureHandlingContinue [16] FailureHandlingContinue OPTIONAL,
    serviceIdentifier [17] INTEGER OPTIONAL,
    aFRecordInformation [19] SEQUENCE OF AFRecordInformation OPTIONAL,
    userLocationInformation [20] UserLocationInformation OPTIONAL,
    eventCounter [24] INTEGER OPTIONAL,
    l7UpVolume [25] INTEGER OPTIONAL,
    l7DownVolume [26] INTEGER OPTIONAL,
    attemptCounter [27] INTEGER OPTIONAL,
    serviceChargeType [28] ServiceChargeType OPTIONAL,
    threeGPP2UserLocationInformation [29] ThreeGPP2UserLocationInformation OPTIONAL,
    sponsorIdentity [30] OCTET STRING (SIZE(1..128)) OPTIONAL,
    applicationServiceProviderIdentity [31] OCTET STRING (SIZE(1..128)) OPTIONAL,
    uWANUserLocationInformation [32] UWANUserLocationInfo OPTIONAL,
    servingPLMNRateControl [35] ServingPLMNRateControl OPTIONAL,
    aPNRateControl [36] APNRateControl OPTIONAL,
    url [100] IA5String(SIZE(1..64)) OPTIONAL,
    serviceConditionChangeEx [101] ServiceConditionChangeEx OPTIONAL
}
```

### 3.2.28 ChangeLocation

```asn1
ChangeLocation ::= SEQUENCE
{
    locationAreaCode [0] LocationAreaCode OPTIONAL,
    routingAreaCode [1] RoutingAreaCode OPTIONAL,
    cellID [2] CellID OPTIONAL,
    changeTime [3] TimeStamp OPTIONAL
}
```

### 3.2.29 ChargingCharacteristics

```asn1
ChargingCharacteristics ::= OCTET STRING(SIZE(2))
```

Format (2 octets):
- Octet 1: B4-B1, N, P, F, H
- Octet 2: B12-B5

Where:
- H: Hot Billing = '00000001'B
- F: Flat Rate = '00000010'B
- P: Prepaid Service = '00000100'B
- N: Normal Billing = '00001000'B
- Bx: Operator-specific behaviors

### 3.2.30 ChargingID

```asn1
ChargingID ::= INTEGER (0..4294967295)
```

### 3.2.31 ChargingRuleBaseName

```asn1
ChargingRuleBaseName ::= IA5String (SIZE(1..36))
```

### 3.2.32 ChChSelectionMode

```asn1
ChChSelectionMode ::= ENUMERATED
{
    servingNodeSupplied (0), -- For S-GW/P-GW
    subscriptionSpecific (1), -- For SGSN only
    aPNSpecific (2), -- For SGSN only
    homeDefault (3), -- For SGSN, S-GW and P-GW
    roamingDefault (4), -- For SGSN, S-GW and P-GW
    visitingDefault (5) -- For SGSN, S-GW and P-GW
}
```

### 3.2.33 ConsolidationResult

```asn1
ConsolidationResult ::= ENUMERATED
{
    Normal (0),
    NotNormal (1),
    ForInterSGSNConSolidation (2),
    ReachLimit (3),
    onlyOneCDRGenerated (4)
}
```

### 3.2.34 ContentChargeInformation

```asn1
ContentChargeInformation ::= SEQUENCE
{
    serviceCode [0] INTEGER OPTIONAL,
    upVolume [1] INTEGER OPTIONAL,
    downVolume [2] INTEGER OPTIONAL,
    qosNegotiated [3] QoSInformation OPTIONAL,
    usageDuration [4] INTEGER OPTIONAL,
    ratingGroup [5] RatingGroupId OPTIONAL,
    rusultCode [6] ResultCode OPTIONAL,
    timeOfFirstUsage [7] TimeStamp OPTIONAL,
    timeOfLastUsage [8] TimeStamp OPTIONAL,
    serviceConditionChange [9] ServiceConditionChange OPTIONAL,
    timeOfReport [10] TimeStamp OPTIONAL,
    failureHandlingContinue [11] FailureHandlingContinue OPTIONAL,
    eventCounter [12] INTEGER OPTIONAL,
    l7UpVolume [13] INTEGER OPTIONAL,
    l7DownVolume [14] INTEGER OPTIONAL,
    attemptCounter [15] INTEGER OPTIONAL,
    serviceChargeType [16] ServiceChargeType OPTIONAL,
    userLocationInformation [17] UmtsUserLocationInformation OPTIONAL,
    ePCQoSInformation [18] EPCQoSInformation OPTIONAL,
    ePCUserLocationInformation [19] UserLocationInformation OPTIONAL,
    url [100] IA5String(SIZE(1..64)) OPTIONAL
}
```

### 3.2.35 ContentExtensions

```asn1
ContentExtensions::= SEQUENCE
{
    extensionType [0] ExtensionType OPTIONAL,
    extensionInformation [1] ExtensionInformation OPTIONAL
}
```

### 3.2.36 CPCIoTEPSOptimisationIndicator

```asn1
CPCIoTEPSOptimisationIndicator ::= BOOLEAN
```

### 3.2.37 CSGAccessMode

```asn1
CSGAccessMode ::= ENUMERATED
{
    closedMode (0),
    hybridMode (1)
}
```

### 3.2.38 CSGId

```asn1
CSGId ::= OCTET STRING (SIZE(4..8))
```

### 3.2.39 CTC-ChangeOfCharCondition

```asn1
CTC-ChangeOfCharCondition ::= SEQUENCE
{
    dataVolumeGPRSUplink [3] DataVolumeGPRS OPTIONAL,
    dataVolumeGPRSDownlink [4] DataVolumeGPRS OPTIONAL,
    changeCondition [5] ChangeCondition OPTIONAL,
    changeTime [6] TimeStamp OPTIONAL,
    ePCUserLocationInformation [8] UserLocationInformation OPTIONAL, -- not support
    ePCQoSInformation [9] EPCQoSInformation OPTIONAL
}
```

### 3.2.40 CTEID

```asn1
CTEID::= OCTET STRING (SIZE(4))
```

### 3.2.41 DataVolumeGPRS

```asn1
DataVolumeGPRS ::= INTEGER
```

### 3.2.42 DataVolumeMBMS

```asn1
DataVolumeMBMS ::= INTEGER
```

### 3.2.43 DefaultGPRS-Handling

```asn1
DefaultGPRS-Handling ::= ENUMERATED
{
    continueTransaction (0),
    releaseTransaction (1)
}
```

### 3.2.44 DefaultSMS-Handling

```asn1
DefaultSMS-Handling ::= ENUMERATED
{
    continueTransaction (0),
    releaseTransaction (1)
}
```

### 3.2.45 DeferredLocationEventType

```asn1
DeferredLocationEventType ::= BIT STRING
{
    msAvailable (0),
    enteringIntoArea (1),
    leavingFromArea (2),
    beingInsideArea (3),
    periodicLDR (4)
} (SIZE (1..16))
```

### 3.2.49 EPCQoSInformation

```asn1
EPCQoSInformation ::= SEQUENCE
{
    -- See TS 29.212 [88] for more information
    qCI [1] INTEGER OPTIONAL,
    maxRequestedBandwithUL [2] INTEGER OPTIONAL,
    maxRequestedBandwithDL [3] INTEGER OPTIONAL,
    guaranteedBitrateUL [4] INTEGER OPTIONAL,
    guaranteedBitrateDL [5] INTEGER OPTIONAL,
    aRP [6] INTEGER OPTIONAL,
    aPNAggregateMaxBitrateUL [7] INTEGER OPTIONAL,
    aPNAggregateMaxBitrateDL [8] INTEGER OPTIONAL,
    extendedMaxRequestedBWUL [9] INTEGER OPTIONAL,
    extendedMaxRequestedBWDL [10] INTEGER OPTIONAL,
    extendedGBRUL [11] INTEGER OPTIONAL,
    extendedGBRDL [12] INTEGER OPTIONAL,
    extendedAPNAMBRUL [13] INTEGER OPTIONAL,
    extendedAPNAMBRDL [14] INTEGER OPTIONAL
}
```

### 3.2.50 ESN

```asn1
ESN ::= OCTET STRING (SIZE(1..15))
```

### 3.2.51 ETSIAddress

```asn1
ETSIAddress ::= AddressString
```

### 3.2.52 Ext-GeographicalInformation

```asn1
Ext-GeographicalInformation ::= OCTET STRING (SIZE (1..maxExt-GeographicalInformation))
maxExt-GeographicalInformation INTEGER ::= 91
```

Refers to geographical Information defined in 3GPP TS 23.032. Composed of 1 or more octets with internal structure according to 3GPP TS 23.032:
- Octet 1: Type of shape (allowed shapes include ellipsoid point variations, arc, polygon)
- Octets 2-n: Shape-specific geographic data (latitude, longitude, uncertainty, etc.)

### 3.2.53 ExtensionInformation

```asn1
ExtensionInformation ::= CHOICE
{
    contentCharge [0] SEQUENCE OF ContentChargeInformation
}
```

### 3.2.54 ExtensionType

```asn1
ExtensionType ::= ENUMERATED
{
    contentCharge (1)
}
```

### 3.2.55 FailureHandlingContinue

```asn1
FailureHandlingContinue ::= BOOLEAN
```

### 3.2.56 FFDAppendIndicator

```asn1
FFDAppendIndicator ::= BOOLEAN
```

### 3.2.57 FreeFormatData

```asn1
FreeFormatData ::= OCTET STRING (SIZE(1..160))
```

### 3.2.58 GSMQoSInformation

```asn1
GSMQoSInformation ::=SEQUENCE
{
    reliability [0] QoSReliability OPTIONAL,
    delay [1] QoSDelay OPTIONAL,
    precedence [2] QoSPrecedence OPTIONAL,
    peakThroughput [3] QoSPeakThroughput OPTIONAL,
    meanThroughput [4] QoSMeanThroughput OPTIONAL
}
```

### 3.2.59 GSNAddress

```asn1
GSNAddress ::= IPAddress
```

### 3.2.60 HardwareID

```asn1
HardwareID::= CHOICE
{
    eSN [1] ESN,
    mEID [2] MEID
}
```

### 3.2.61 HSGWChange

```asn1
HSGWChange ::= BOOLEAN
```

### 3.2.62 IMEI

```asn1
IMEI ::= TBCD-STRING (SIZE (1..8))
```

### 3.2.63 IMSI

```asn1
IMSI ::= TBCD-STRING (SIZE (1..8))
```

### 3.2.64 IPAddress

```asn1
IPAddress ::= CHOICE
{
    iPBinaryAddress IPBinaryAddress,
    iPTextRepresentedAddress IPTextRepresentedAddress
}
```

### 3.2.65 IPBinaryAddress

```asn1
IPBinaryAddress ::= CHOICE
{
    iPBinV4Address [0] IPBinV4Address,
    iPBinV6Address IPBinV6AddressWithOrWithoutPrefixLength
}

IPBinV4Address ::= OCTET STRING (SIZE(4))
IPBinV6Address ::= OCTET STRING (SIZE(16))

IPBinV6AddressWithOrWithoutPrefixLength ::= CHOICE
{
    iPBinV6Address [1] IPBinV6Address,
    iPBinV6AddressWithPrefix [4] IPBinV6AddressWithPrefixLength
}

IPBinV6AddressWithPrefixLength ::= SEQUENCE
{
    iPBinV6Address [1] IPBinV6Address OPTIONAL,
    pDPAddressPrefixLength [2] PDPAddressPrefixLength OPTIONAL
}

PDPAddressPrefixLength ::= INTEGER (1..64)
-- This is an integer indicating the length of the PDP/PDN IPv6 address prefix
-- and the default value is 64 bits.
```

### 3.2.66 IPPort

```asn1
IPPort ::= OCTET STRING (SIZE(2))
```
Format: 2 octets with MSB first.

### 3.2.67 IPTextRepresentedAddress

```asn1
IPTextRepresentedAddress ::= CHOICE
{
    iPTextV4Address [2] IA5String (SIZE(7..15)),
    iPTextV6Address [3] IA5String (SIZE(15..45))
}
```

### 3.2.68 ISDN-AddressString

```asn1
ISDN-AddressString ::= AddressString
```

### 3.2.69 LCSCause

```asn1
LCSCause ::= OCTET STRING (SIZE(1))
```

LCS Cause values (bits 8-1):
- 00000000 = Unspecified
- 00000001 = System Failure
- 00000010 = Protocol Error
- 00000011 = Data missing in position request
- 00000100 = Unexpected data value in position request
- 00000101 = Position method failure
- 00000110 = Target MS Unreachable
- 00000111 = Location request aborted
- 00001000 = Facility not supported
- 00001001 = Inter-BSC Handover Ongoing
- 00001010 = Intra-BSC Handover Complete
- 00001011 = Congestion
- 00001100 = Inter NSE cell change
- 00001101 = Routing Area Update
- 00001110 = PTMSI reallocation
- 00001111 = Suspension of GPRS services

### 3.2.70 LCSClientExternalID

```asn1
LCSClientExternalID ::= SEQUENCE {
    externalAddress [0] ISDN-AddressString OPTIONAL
}
```

### 3.2.71 LCSClientInternalID

```asn1
LCSClientInternalID ::= ENUMERATED
{
    broadcastService (0),
    o-andM-HPLMN (1),
    o-andM-VPLMN (2),
    anonymousLocation (3),
    targetMSsubscribedService (4)
}
```

### 3.2.72 LCSClientIdentity

```asn1
LCSClientIdentity ::= SEQUENCE
{
    lcsClientExternalID [0] LCSClientExternalID OPTIONAL,
    lcsClientDialedByMS [1] AddressString OPTIONAL,
    lcsClientInternalID [2] LCSClientInternalID OPTIONAL
}
```

### 3.2.73 LCSClientType

```asn1
LCSClientType ::= ENUMERATED
{
    emergencyServices (0),
    valueAddedServices (1),
    plmnOperatorServices (2),
    lawfulInterceptServices (3)
}
```

### 3.2.74 LCSLocationAreaCode

```asn1
LCSLocationAreaCode ::= OCTET STRING (SIZE(9))
```

Format (9 octets):
- Octets 1-3: MCC/MNC
- Octets 4-5: LAC (Location Area Code)
- Octets 6-7: CI (Cell Identity)
- Octets 8-9: SAC (Service Area Code)

### 3.2.75 LCS-Priority

```asn1
LCS-Priority ::= OCTET STRING (SIZE (1))
-- 0 = highest priority
-- 1 = normal priority
-- all other values treated as 1
```

### 3.2.76 LCSQoSInfo

```asn1
LCSQoSInfo ::= OCTET STRING (SIZE(4))
```

Format (4 octets):
- Octet 1: spare, VEL (velocity), VERT (vertical coordinate)
- Octet 2: HA (horizontal accuracy indicator), Horizontal Accuracy value
- Octet 3: VA (vertical accuracy indicator), Vertical Accuracy value
- Octet 4: RT (response time category), spare

### 3.2.77 LevelOfCAMELService

```asn1
LevelOfCAMELService ::= BIT STRING
{
    basic (0),
    callDurationSupervision (1),
    onlineCharging (2)
}
```

### 3.2.78 LocationAreaAndCell

```asn1
LocationAreaAndCell ::= SEQUENCE
{
    locationAreaCode [0] LCSLocationAreaCode OPTIONAL
}
```

### 3.2.79 LocationAreaCode

```asn1
LocationAreaCode ::= OCTET STRING (SIZE(2))
```
Format: 2 octets, MSB first.

### 3.2.80 LocationEstimateType

```asn1
LocationEstimateType ::= ENUMERATED
{
    currentLocation (0),
    currentOrLastKnownLocation (1),
    initialLocation (2),
    activateDeferredLocation (3),
    cancelDeferredLocation (4),
    notificationVerificationOnly (5)
}
```

### 3.2.81 LocationMethod

```asn1
LocationMethod::= ENUMERATED
{
    msBasedEOTD (0),
    msAssistedEOTD (1),
    assistedGPS (2),
    msBasedOTDOA (3),
    assistedGANSS (4),
    assistedGPSandGANSS (5)
}
```

### 3.2.82 LocationType

```asn1
LocationType ::= SEQUENCE
{
    locationEstimateType [0] LocationEstimateType OPTIONAL,
    deferredLocationEventType [1] DeferredLocationEventType OPTIONAL
}
```

### 3.2.83 MBMSInformation

```asn1
MBMSInformation ::= SET
{
    tMGI [1] TMGI OPTIONAL,
    mBMSSessionIdentity [2] MBMSSessionIdentity OPTIONAL,
    mBMSServiceType [3] MBMSServiceType OPTIONAL,
    mBMSUserServiceType [4] MBMSUserServiceType OPTIONAL, -- only supported in BM-SC
    mBMS2G3GIndicator [5] MBMS2G3GIndicator OPTIONAL, -- supported in GERAN and UTRAN
    fileRepairSupported [6] BOOLEAN OPTIONAL, -- only supported in BM-SC
    rAI [7] RoutingAreaCode OPTIONAL, -- only supported in BM-SC
    mBMSServiceArea [8] MBMSServiceArea OPTIONAL,
    requiredMBMSBearerCaps [9] RequiredMBMSBearerCapabilities OPTIONAL,
    mBMSGWAddress [10] GSNAddress OPTIONAL,
    cNIPMulticastDistribution [11] CNIPMulticastDistribution OPTIONAL,
    mBMSAccessIndicator [12] MBMSAccessIndicator OPTIONAL -- supported in UTRAN and eUTRAN
}
```

#### MBMS Related Types

```asn1
MBMS2G3GIndicator ::= ENUMERATED
{
    is2G (0), -- For GERAN access only
    is3G (1), -- For UTRAN access only
    is2G-AND-3G (2) -- For both UTRAN and GERAN access
}

MBMSServiceType ::= ENUMERATED
{
    mULTICAST (0),
    bROADCAST (1)
}

MBMSUserServiceType ::= ENUMERATED
{
    dOWNLOAD (0),
    sTREAMING (1)
}

RequiredMBMSBearerCapabilities ::= OCTET STRING (SIZE (3..14))
-- Formatted with QoS octets including traffic class, delivery order, etc.

MBMSSessionIdentity ::= OCTET STRING (SIZE (1))

TMGI ::= OCTET STRING (SIZE(6))
-- Format: Octets 1-3: MBMS Service ID
-- Octets 4-6: MCC/MNC

MBMSServiceArea::= OCTET STRING(SIZE (1..513))

CNIPMulticastDistribution ::= ENUMERATED
{
    nO-IP-MULTICAST (0),
    iP-MULTICAST (1)
}

MBMSAccessIndicator ::= ENUMERATED
{
    uTRAN (0),
    eUTRAN (1),
    uTRAN-AND-EUTRAN (2)
}
```

### 3.2.84 ManagementExtension

```asn1
ManagementExtension ::=INTEGER
```

### 3.2.85 ManagementExtensions

```asn1
ManagementExtensions ::= INTEGER
```

### 3.2.86 MEID

```asn1
MEID ::= OCTET STRING (SIZE(1..14))
```

### 3.2.87 MessageReference

```asn1
MessageReference ::= OCTET STRING(SIZE(1..2))
```

### 3.2.88 MOExceptionDataCounter

```asn1
MOExceptionDataCounter ::= SEQUENCE
{
    counterValue [0] INTEGER,
    counterTimestamp [1] TimeStamp
}
```

### 3.2.89 MSISDN

```asn1
MSISDN ::= ISDN-AddressString
```

### 3.2.90 MSNetworkCapability

```asn1
MSNetworkCapability ::= OCTET STRING (SIZE(1..8))
```

### 3.2.91 MSTimeZone

```asn1
MSTimeZone ::= OCTET STRING (SIZE (2))
```

Format:
- Octet 1: Time Zone (offset from UTC in 15-minute steps)
    - Bit 4: algebraic sign (0=positive, 1=negative)
    - Bits 1-3: tens digit
    - Bits 5-8: ones digit
- Octet 2: Daylight saving time adjustment (bits 1-2)
    - 00 = No adjustment
    - 01 = +1 hour adjustment
    - 10 = +2 hours adjustment

### 3.2.92 NetworkInitiatedPDPContext

```asn1
NetworkInitiatedPDPContext ::= BOOLEAN
```

### 3.2.93 NodeID

```asn1
NodeID ::= IA5String (SIZE(1..20))
```

### 3.2.94 NotificationToMSUser

```asn1
NotificationToMSUser ::= ENUMERATED
{
    notifyLocationAllowed (0),
    notifyAndVerify-LocationAllowedIfNoResponse (1),
    notifyAndVerify-LocationNotAllowedIfNoResponse (2),
    locationNotAllowed (3)
}
```

### 3.2.95 PDNConnectionID

```asn1
PDNConnectionID ::= INTEGER
```

### 3.2.96 NumberOfDPEncountered

```asn1
NumberOfDPEncountered ::= INTEGER
```

### 3.2.97 PDPAddress

```asn1
PDPAddress ::= CHOICE
{
    iPAddress [0] IPAddress,
    eTSIAddress [1] ETSIAddress
}
```

### 3.2.98 PDPType

```asn1
PDPType ::= OCTET STRING (SIZE(2))
```

Format:
- Octet 1: PDP Type Organization (0=ETSI, 1=IETF)
- Octet 2: PDP Type Number (0x21=IPv4, 0x57=IPv6, 0x8D=IPv4/IPv6)

### 3.2.99 PDPPDNTypeExtension

```asn1
PDPPDNTypeExtension ::= INTEGER
{
    ipv4 (0),
    ppp (1),
    ipv6 (2),
    ipv4v6 (3),
    nonIP (4)
}
```

### 3.2.100 PlmnId

```asn1
PlmnId::= OCTET STRING (SIZE(3))
```
Format: 3 octets containing MCC (Mobile Country Code) and MNC (Mobile Network Code)

### 3.2.101 PositioningData

```asn1
PositioningData ::= OCTET STRING (SIZE (1..33))
```

This provides positioning data for location attempts. Format includes:
- Octet 1: Positioning Data Discriminator
- Octets 2+: Positioning method data

### 3.2.102 PositionMethodFailure-Diagnostic

```asn1
PositionMethodFailure-Diagnostic ::= ENUMERATED
{
    congestion (0),
    insufficientResources (1),
    insufficientMeasurementData (2),
    inconsistentMeasurementData (3),
    locationProcedureNotCompleted (4),
    locationProcedureNotSupportedByTargetMS (5),
    qoSNotAttainable (6),
    positionMethodNotAvailableInNetwork (7),
    positionMethodNotAvailableInLocationArea (8)
}
```

### 3.2.103 PSFurnishChargingInformation

```asn1
PSFurnishChargingInformation ::= SEQUENCE
{
    pSFreeFormatData [1] FreeFormatData OPTIONAL,
    pSFFDAppendIndicator [2] FFDAppendIndicator OPTIONAL
}
```

### 3.2.104 QoSDelay

```asn1
QoSDelay ::= ENUMERATED
{
    subscribedDelayClass (0),
    delayClass1 (1),
    delayClass2 (2),
    delayClass3 (3),
    delayClass4 (4),
    reserved5 (5),
    reserved6 (6),
    reserved7 (7)
}
```

### 3.2.105 QoSInformation

```asn1
QoSInformation ::= CHOICE
{
    -- gsmQosInformation and umtsQosInformation used in S-CDR,G-CDR,S-MB-CDR,G-MB-CDR only
    -- ePCQoSInformation used in SGW-CDR,PGW-CDR,GWMBMS only
    gsmQosInformation [0] GSMQoSInformation,
    umtsQosInformation [1] UmtsQosInformation,
    ePCQoSInformation [2] EPCQoSInformation
}
```

### 3.2.106 QoSMeanThroughput

```asn1
QoSMeanThroughput ::= ENUMERATED
{
    subscribedMeanThroughput (0),
    mean100octetPh (1),
    mean200octetPh (2),
    mean500octetPh (3),
    mean1000octetPh (4),
    mean2000octetPh (5),
    mean5000octetPh (6),
    mean10000octetPh (7),
    mean20000octetPh (8),
    mean50000octetPh (9),
    mean100000octetPh (10),
    mean200000octetPh (11),
    mean500000octetPh (12),
    mean1000000octetPh (13),
    mean2000000octetPh (14),
    mean5000000octetPh (15),
    mean10000000octetPh (16),
    mean20000000octetPh (17),
    mean50000000octetPh (18),
    reserved19 (19),
    reserved20 (20),
    reserved21 (21),
    reserved22 (22),
    reserved23 (23),
    reserved24 (24),
    reserved25 (25),
    reserved26 (26),
    reserved27 (27),
    reserved28 (28),
    reserved29 (29),
    reserved30 (30),
    bestEffort (31)
}
```

### 3.2.107 QoSPeakThroughput

```asn1
QoSPeakThroughput ::= ENUMERATED
{
    subscribedPeakThroughput (0),
    upTo1000octetPs (1),
    upTo2000octetPs (2),
    upTo4000octetPs (3),
    upTo8000octetPs (4),
    upTo16000octetPs (5),
    upTo32000octetPs (6),
    upTo64000octetPs (7),
    upTo128000octetPs (8),
    upTo256000octetPs (9),
    reserved10 (10),
    reserved11 (11),
    reserved12 (12),
    reserved13 (13),
    reserved14 (14),
    reserved15 (15)
}
```

### 3.2.108 QoSPrecedence

```asn1
QoSPrecedence ::= ENUMERATED
{
    subscribedPrecedence (0),
    highPriority (1),
    normalPriority (2),
    lowPriority (3),
    reserved4 (4),
    reserved5 (5),
    reserved6 (6),
    reserved7 (7)
}
```

### 3.2.109 QoSReliability

```asn1
QoSReliability ::= ENUMERATED
{
    subscribedReliabilityClass (0),
    acknowledgedGTP (1),
    unackGTPAcknowLLC (2),
    unackGTPLLCAcknowRLC (3),
    unackGTPLLCRLC (4),
    unacknowUnprotectedData (5),
    reserved6 (6),
    reserved7 (7)
}
```

### 3.2.110 RAIdentity

```asn1
RAIdentity ::= OCTET STRING (SIZE (6))
```
Format: 6 octets containing MCC/MNC (3 octets), LAC (2 octets), RAC (1 octet)

### 3.2.111 RANSecondaryRATUsageReport

```asn1
RANSecondaryRATUsageReport ::= SEQUENCE
{
    dataVolumeUplink [1] DataVolumeGPRS,
    dataVolumeDownlink [2] DataVolumeGPRS,
    rANStartTime [3] TimeStamp,
    rANEndTime [4] TimeStamp,
    secondaryRATType [5] SecondaryRATType
}
```

### 3.2.112 RatingGroupId

```asn1
RatingGroupId ::= INTEGER
-- IP service flow identity (DCCA)
-- range of 4 byte (0..2147483647 / negative numbers not used)
```

### 3.2.113 RateControlTimeUnit

```asn1
RateControlTimeUnit ::= INTEGER
{
    unrestricted (0),
    minute (1),
    hour (2),
    day (3),
    week (4)
}
```

### 3.2.114 RATType

```asn1
RATType ::= INTEGER
{
    reserved (0),
    uTRAN (1),
    gERAN (2),
    wLAN (3),
    gAN (4),
    hSPAEvolution (5),
    eUTRAN (6),
    virtual (7),
    eUTRAN-NB-IoT (8),
    iEEE802-16e (101),
    eHRPDfor3GPP2 (102),
    hRPD3GPP2 (103),
    onexRTTfor3GPP2 (104),
    uMBfor3GPP2 (105)
}
```

### 3.2.115 RecordingEntity

```asn1
RecordingEntity ::= AddressString
```

### 3.2.116 RecordSeqNumber

```asn1
RecordSeqNumber ::= OCTET STRING(SIZE(3))
```

### 3.2.117 ResultCode

```asn1
ResultCode ::= INTEGER
-- online charging protocol return value (DCCA)
-- range of 4 byte (0..4294967259)
-- = 2001 – Diameter success
-- = 4010 – Diameter end user service denied (terminate category)
-- = 4011 – Diameter credit control not applicable
-- = 4012 – Diameter credit limit reached
-- = 5003 – Diameter authorization rejected (terminate PDP context)
-- = 5003 – Diameter authorization rejected (blacklist category)
-- = 5030 – Diameter user unknown
-- = 5031 – Diameter rating failed (category not recognized)
```

### 3.2.118 RncID

```asn1
RncID ::= SEQUENCE
{
    plmnId [0] PlmnId OPTIONAL,
    rncId [1] INTEGER (0..4095) OPTIONAL
}
```

### 3.2.119 RoutingAreaCode

```asn1
RoutingAreaCode ::= OCTET STRING (SIZE(1..2))
```

### 3.2.120 SCFAddress

```asn1
SCFAddress ::= ISDN-AddressString
```

### 3.2.121 SCSASAddress

```asn1
SCSASAddress ::= SET
{
    sCSAddress [1] IPAddress,
    sCSRealm [2] DiameterIdentity -- not supported yet
}
```

### 3.2.122 SecondaryRATType

```asn1
SecondaryRATType ::= INTEGER
{
    reserved (0),
    nR (1) -- New Radio 5G
}
```

### 3.2.123 Served3GPP2MEID

```asn1
Served3GPP2MEID ::= OCTET STRING(SIZE(1..14))
```

### 3.2.124 ServiceChargeType

```asn1
ServiceChargeType ::= ENUMERATED
{
    volume (0),
    time (1),
    volumeAndTime (2),
    event (3),
    timeAndEvent (4),
    volumeAndEvent (5),
    volumeAndTimeAndEvent (6)
}
```

### 3.2.125 ServiceConditionChange

```asn1
ServiceConditionChange ::= OCTET STRING (SIZE(4..5))
```

Bitmap format (4-5 octets) with bit definitions:
- Bit 1: qoSChange
- Bit 2: sGSNChange
- Bit 3: sGSNPLMNIDChange
- Bit 4: tariffTimeSwitch
- Bit 5: pDPContextRelease
- Bit 6: rATChange
- Bit 7: serviceIdledOut
- Bit 8: qCTExpiry
- Bit 9: configurationChange
- Bit 10: serviceStop
- Bits 11-38: Various DCCA and change conditions

### 3.2.126 ServiceConditionChangeEx

```asn1
ServiceConditionChangeEx ::= BIT STRING
{
    qoSChange (0),
    sGSNChange (1),
    sGSNPLMNIDChange (2),
    tariffTimeSwitch (3),
    pDPContextRelease (4),
    rATChange (5),
    serviceIdledOut (6),
    qCTExpiry (7), -- old: QCTexpiry is no report event
    configurationChange (8),
    serviceStop (9),
    dCCATimeThresholdReached (10),
    dCCAVolumeThresholdReached (11),
    dCCAServiceSpecificUnitThresholdReached (12),
    dCCATimeExhausted (13),
    dCCAVolumeExhausted (14),
    dCCAValidityTimeout (15),
    returnRequested (16), -- reserved due to no use case
    dCCAReauthorisationRequest (17),
    dCCAContinueOngoingSession (18),
    dCCARetryAndTerminateOngoingSession (19),
    dCCATerminateOngoingSession (20),
    cGI-SAIChange (21),
    rAIChange (22),
    dCCAServiceSpecificUnitExhausted (23),
    recordClosure (24),
    timeLimit (25),
    volumeLimit (26),
    serviceSpecificUnitLimit (27),
    envelopeClosure (28),
    eCGIChange (29),
    tAIChange (30),
    userLocationChange (31),
    userCSGInformationChange (32),
    presenceInPRAChange (33),
    accessChangeOfSDF (34),
    indirectServiceConditionChange (35),
    servingPLMNRateControlChange (36),
    aPNRateControlChange (37)
}
```

### 3.2.127 ServiceKey

```asn1
ServiceKey ::= OCTET STRING(SIZE(1..5))
```
Format: 1-5 octets, MSB first

### 3.2.128 ServingNodeType

```asn1
ServingNodeType ::= ENUMERATED
{
    sGSN (0),
    pMIPSGW (1),
    gTPSGW (2),
    ePDG (3),
    hSGW (4),
    mME (5),
    tWAN (6),
    ePCF (100)
}
```

### 3.2.129 ServingPLMNRateControl

```asn1
ServingPLMNRateControl ::= SEQUENCE
{
    sPLMNDLRateControlValue [0] INTEGER,
    sPLMNULRateControlValue [1] INTEGER
}
```

### 3.2.130 SGiPtPTunnellingMethod

```asn1
SGiPtPTunnellingMethod ::= ENUMERATED
{
    uDPIPbased (0),
    others (1)
}
```

### 3.2.131 SGSNChange

```asn1
SGSNChange ::= BOOLEAN
```

### 3.2.132 SGWChange

```asn1
SGWChange ::= BOOLEAN
```

### 3.2.133 SMSResult

```asn1
SMSResult ::= Diagnostics
```

### 3.2.134 SmsTpDestinationNumber

```asn1
SmsTpDestinationNumber ::= BCDDirectoryNumber
```

### 3.2.135 SUBNET

```asn1
SUBNET ::= OCTET STRING (SIZE(1..37))
```

### 3.2.136 SubscriptionID

```asn1
SubscriptionID ::= SET
{
    subscriptionIDType [1] SubscriptionIDType OPTIONAL,
    subscriptionIDData [2] SubscriptionIDData OPTIONAL
}
```

### 3.2.137 SubscriptionIDData

```asn1
SubscriptionIDData ::= OCTET STRING (SIZE(1..64))
```

### 3.2.138 SubscriptionIDType

```asn1
SubscriptionIDType ::= ENUMERATED
{
    eND-USER-E164 (0),
    eND-USER-IMSI (1),
    eND-USER-SIP-URI (2),
    eND-USER-NAI (3),
    eND-USER-PRIVATE (4)
}
```

### 3.2.139 SystemType

```asn1
SystemType ::= ENUMERATED
{
    unknown (0),
    iuUTRAN (1),
    gERAN (2)
}
```

### 3.2.140 TBCD-STRING

```asn1
TBCD-STRING ::= OCTET STRING
```

### 3.2.141 ThreeGPP2UserLocationInformation

```asn1
ThreeGPP2UserLocationInformation ::= OCTET STRING (SIZE(1..37))
```

### 3.2.142 TimeStamp

```asn1
TimeStamp ::= OCTET STRING (SIZE(9))
```

Format (9 octets) indicating local time in UTC compact form:
- Octet 1: Year (00-99 BCD encoded)
- Octet 2: Month (01-12 BCD encoded)
- Octet 3: Day (01-31 BCD encoded)
- Octet 4: Hour (00-23 BCD encoded)
- Octet 5: Minute (00-59 BCD encoded)
- Octet 6: Second (00-59 BCD encoded)
- Octet 7: Sign ("+" or "-" ASCII encoded)
- Octets 8-9: Time zone offset (hours and minutes BCD)

### 3.2.143 TWANUserLocationInfo

```asn1
TWANUserLocationInfo ::= SEQUENCE
{
    sSID [0] OCTET STRING (SIZE(1..32)) OPTIONAL,
    bSSID [1] OCTET STRING (SIZE(6)) OPTIONAL
}
```

### 3.2.144 UmtsQosInformation

```asn1
UmtsQosInformation ::= OCTET STRING(SIZE(4..17))
```

Format includes multiple octets encoding:
- Allocation/Retention Priority
- Delay class, Reliability class
- Peak throughput, Precedence class
- Mean throughput
- Traffic Class, Delivery order, Delivery of erroneous SDU
- Maximum SDU size
- Maximum/Guaranteed bit rates for uplink/downlink
- Residual BER, SDU error ratio
- Transfer delay, Traffic Handling priority
- Extended bit rates (if present)

### 3.2.145 UmtsUserLocationInformation

```asn1
UmtsUserLocationInformation ::= OCTET STRING (SIZE(8))
```

Format:
- Octet 1: Geographic Location Type (0=CGI, 1=SAI, 2=RAI)
- Octets 2-8: Geographic Location
    - For CGI: MCC/MNC (3 octets), LAC (2 octets), CI (2 octets)
    - For SAI: MCC/MNC (3 octets), LAC (2 octets), SAC (2 octets)
    - For RAI: MCC/MNC (3 octets), LAC (2 octets), RAC (2 octets)

### 3.2.146 UnauthorizedLCSClient-Diagnostic

```asn1
UnauthorizedLCSClient-Diagnostic ::= ENUMERATED
{
    noAdditionalInformation (0),
    clientNotInMSPrivacyExceptionList (1),
    callToClientNotSetup (2),
    privacyOverrideNotApplicable (3),
    disallowedByLocalRegulatoryRequirements (4),
    unauthorizedPrivacyClass (5),
    unauthorizedCallSessionUnrelatedExternalClient (6),
    unauthorizedCallSessionRelatedExternalClient (7)
}
```

### 3.2.147 UNIPDUCPOnlyFlag

```asn1
UNIPDUCPOnlyFlag::= BOOLEAN
```

### 3.2.148 UserCSGInformation

```asn1
UserCSGInformation ::= SEQUENCE
{
    cSGId [0] CSGId OPTIONAL,
    cSGAccessMode [1] CSGAccessMode OPTIONAL,
    cSGMembershipIndication [2] NULL OPTIONAL
}
```

### 3.2.149 UserLocationInformation

```asn1
UserLocationInformation ::= OCTET STRING (SIZE(6..34))
```

Format with flags and location data:
- Octet 1: Flags (ECGI, TAI, RAI, SAI, CGI presence indicators)
- Subsequent octets: Location data in order CGI, SAI, RAI, TAI, ECGI (if present)
  Each location type includes MCC/MNC and specific area codes.

### 3.2.150 UWANUserLocationInfo

```asn1
UWANUserLocationInfo ::= SEQUENCE
{
    uELocalIPAddress [0] IPAddress OPTIONAL,
    uDPSourcePort [1] IPPort OPTIONAL
}
```

## 3.3 TAG Values

This section defines the ASN.1 TAG values for each field in the CDR structures.

### 3.3.1 CallEventRecord

| TAG Value | Description |
|-----------|-------------|
| H'B4 | SGSNPDPRecord |
| H'B5 | GGSNPDPRecord |
| H'B6 | SGSNMMRecord |
| H'B7 | SGSNSMORecord |
| H'B8 | SGSNSMTRecord |
| H'B9 | SGSNMTLCSRecord |
| H'BA | SGSNMOLCSRecord |
| H'BB | SGSNNILCSRecord |
| H'BD | SGSNMBMSRecord |
| H'BE | GGSNMBMSRecord |
| H'BF4E | SGWRecord |
| H'BF4F | PGWRecord |
| H'BF50 | WLANRecord |
| H'BF56 | GWMBMSRecord |
| H'BF60 | ePDGRecord |
| H'BF8148 | hSGWRecord |

### 3.3.2 SGSNPDPRecord TAG Values

| TAG Value | Description |
|-----------|-------------|
| H'80 | recordType |
| H'81 | networkInitiation |
| H'83 | servedIMSI |
| H'84 | servedIMEI |
| H'A5 | sgsnAddressList |
| H'86 | msNetworkCapability |
| H'87 | routingArea |
| H'88 | locationAreaCode |
| H'89 | cellIdentifier |
| H'8A | chargingID |
| H'AB | ggsnAddressUsed |
| H'8C | accessPointNameNI |
| H'8D | pdpType |
| H'AE | servedPDPAddress |
| H'AF | listOfTrafficVolumes |
| H'90 | recordOpeningTime |
| H'91 | duration |
| H'92 | sgsnChange |
| H'93 | causeForRecClosing |
| H'B4 | diagnostics |
| H'B5 | listOfRecordSequenceNumber |
| H'96 | nodeID |
| H'97 | recordExtensions |
| H'98 | localSequenceNumber |
| H'99 | apnSelectionMode |
| H'9A | accessPointNameOI |
| H'9B | servedMSISDN |
| H'9C | chargingCharacteristics |
| H'9D | systemType |
| H'BE | cAMELInformationPDP |

### 3.3.3 GGSNPDPRecord TAG Values

| TAG Value | Description |
|-----------|-------------|
| H'80 | recordType |
| H'81 | networkInitiation |
| H'83 | servedIMSI |
| H'A4 | ggsnAddress |
| H'85 | chargingID |
| H'A6 | sgsnAddress |
| H'87 | accessPointNameNI |
| H'88 | pdpType |
| H'A9 | servedPDPAddress |
| H'8B | dynamicAddressFlag |
| H'AC | listOfTrafficVolumes |
| H'8D | recordOpeningTime |
| H'8E | duration |
| H'8F | causeForRecClosing |
| H'B0 | diagnostics |
| H'B1 | listOfRecordSequenceNumber |
| H'92 | nodeID |
| H'B3 | recordExtensions |
| H'94 | localSequenceNumber |
| H'95 | apnSelectionMode |
| H'96 | servedMSISDN |
| H'97 | chargingCharacteristics |
| H'BA | localSeqNoList |
| H'9B | sgsnPLMNIdentifier |
| H'9C | chChSelectionMode |
| H'9D | rATType |
| H'9E | consolidationResult |
| H'9F20 | iMSsignalingContext |
| H'9F21 | externalChargingID |

### Summary of Additional TAG Tables

The document continues with detailed TAG value tables for all record types including:

- **SGSNMMRecord** (Table 24): Tags H'80 through H'97 for mobility management fields
- **SGSNSMORecord** (Table 25): Tags for SMS originating records
- **SGSNSMTRecord** (Table 26): Tags for SMS terminating records
- **SGSNMOLCSRecord** (Table 27): Tags for location service MO records
- **SGSNMTLCSRecord** (Table 28): Tags for location service MT records
- **SGSNNILCSRecord** (Table 29): Tags for network initiated location service records
- **SGSNMBMSRecord** (Table 30): Tags for MBMS bearer context records
- **GGSNMBMSRecord** (Table 31): Tags for GGSN MBMS records
- **GWMBMSRecord** (Table 32): Tags for MBMS gateway records
- **SGWRecord** (Table 33): Tags H'80 through H'BF65 for serving gateway records
- **PGWRecord** (Table 34): Extensive tags including H'80 through H'9F6B for PDN gateway records
- **WLANRecord** (Table 35): Tags for WLAN PDG records
- **HSGWRecord** (Table 36): Tags for HSGW records
- **EPDGRecord** (Table 37): Tags for ePDG records

Each table maps hexadecimal TAG values to their corresponding field names, including nested structures and optional components.

## 4 ANNEX

### 4.1 Description of Version Number

The version number format is **Vxx.xx.xx** where:

- **First digit**: Incremented when new CDR types are added to the document. When changed, the billing system may need to upgrade if it requires the new CDR type.

- **Second digit**: Incremented when adding new fields to current CDRs or when field definitions change. When changed, the billing system should upgrade its software.

- **Third digit**: Incremented for editorial changes only. When changed, the billing system need not upgrade its software.

---

*End of Document - ZXUN CG CDR ASN.1 Description (v7.2.1) - Part 2*
*This document contains the ASN.1 definitions starting from section 3.1.4 SGSNMMRecord*
