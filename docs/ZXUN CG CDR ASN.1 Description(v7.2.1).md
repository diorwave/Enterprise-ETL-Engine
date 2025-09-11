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
|-------------|-------------|------|---------|---------|
| - | V6.0.0 | 2009-11-3 | Created | ZXUN-eCG V4.10.10.P2 |
| V6.0.0 | V6.1.0 | 2010-10-23 | Add DiameterSessionID to GCDR | ZXUN-eCG V4.10.10.P3 |
| V6.1.0 | V6.2.0 | 2010-11-18 | 1. Add localSequenceNumber,servingNodeAddress,userLocationInformation to ChangeOfServiceCondition<br>2. Add asn.1 description to ServiceConditionChange<br>3. Add DiameterSessionID to PGWCDR<br>4. Add servedPDPPDNAddressExt to GCDR,SGWCDR and PGWCDR<br>5. Add W-CDR | ZXUN-eCG V4.10.10.P4 |
| V6.2.0 | V6.2.1 | 2010-12-15 | Correct the spelling error | ZXUN-eCG V4.10.10.P4 |
| V6.2.1 | V6.2.1 | 2011-1-18 | Add Ipv4/Ipv6 asn.1 description to PDPType field<br>Modify the value of "Best effort" to 31 in QoSMeanThroughput | ZXUN-eCG V4.10.10.P4 |
| V6.2.1 | V6.2.1 | 2011-6-30 | Add RAI description to UmtsUserLocationInformation field | ZXUN-eCG V4.10.10.P7 |
| V6.2.1 | V6.3.0 | 2011-8-11 | 1. Correct the definition of serviceConditionChange<br>2. Correct the definition of url and diametersessionID to IA5String<br>3. Modify tags<br>4. Modify tag from H'9E to H'BE<br>5. Delete tag 0x30<br>6. Modify tag from H'82 to 0x0A<br>7. Modify tag from H'80 to 0x04<br>8. Add iMSIunauthenticatedFlag | ZXUN-CG V4.11.20.Bx |
| V6.3.0 | V6.4.0 | 2012-1-5 | 1. Add servedPDPPDNAddressExt to SCDR<br>2. Add pSFurnishChargingInformation to GCDR | ZXUN-CG V4.11.20.P1 |
| V6.4.0 | V6.4.1 | 2012-3-6 | 1. Add serviceChargeType<br>2. Add userLocationInformation<br>3. Add HSGW-CDR | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.1 | 2012-6-15 | Correct the size of UmtsQosInformation | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.1 | 2012-6-20 | 1. Correct the size of serviceKey<br>2. Add subscribedDelayClass<br>3. Correct definitions | ZXUN-CG V4.11.20.P2 |
| V6.4.1 | V6.4.2 | 2012-7-2 | Add cellPLMNID field to MCDR, SMO and SMT | ZXUN-CG V4.11.20.P3 |
| V6.4.1 | V6.4.2 | 2012-7-12 | Modify sgsnPLMNIdentifier of S-CDR to cellPLMNID | ZXUN-CG V4.11.20.P3 |
| V6.4.2 | V6.4.2 | 2012-8-2 | Correct the description of HSGW-CDR.ServedHardwareID | ZXUN-CG V4.11.20.P3 |
| V6.4.2 | V6.5.0 | 2012-8-28 | 1. Add MBMS-GW-CDR<br>2. Add EPC fields<br>3. Add userCSGInformation | ZXUN-CG V4.12.10.Bx |
| V6.5.0 | V6.5.0 | 2012-9-21 | Modify pdpType of all record type | ZXUN-CG V4.12.10.Bx |
| V6.5.0 | V6.5.1 | 2012-12-13 | Add pSFurnishChargingInformation in PGW-CDR | ZXUN-CG V4.12.10.P1 |
| V6.5.1 | V6.5.1 | 2013-1-17 | Various corrections and improvements | ZXUN-CG V4.12.10.P1 |
| V6.5.1 | V6.5.1 | 2013-1-31 | Correct servedIMSI to OPTIONAL in all CDR | ZXUN-CG V4.12.10.P1 |
| V6.5.2 | V6.5.1 | 2013-4-16 | Modify ASN.1 definition of LCS CDR according to 3GPP R9 | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.2 | 2013-5-27 | Add rATType definitions(101~105) | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.2 | 2013-8-13 | Correct the definition of LCSClientExternalID | ZXUN-CG V4.12.10.P3 |
| V6.5.2 | V6.5.3 | 2013-8-13 | Add ServiceChargeType definitions(3~6) | ZXUN-CG V4.13.10.Bx |
| V6.5.3 | V6.5.3 | 2013-8-29 | Add bit definition of MsTimeZone | ZXUN-CG V4.13.10.Bx |
| V6.5.3 | V6.6.0 | 2013-9-17 | 1. Redefined hSGWRecord<br>2. Correct ASN.1 definition | ZXUN-CG V4.13.10.Bx |
| V6.6.0 | V6.6.0 | 2013-12-10 | Add enumerated value: ePCF (100) in ServingNodeType | ZXUN-CG V4.13.10.Bx |
| V6.6.0 | V6.6.0 | 2014-01-15 | Correct definition of WLAN.servingProxyAddress | ZXUN-CG V4.13.10.P1 |
| V6.6.0 | V6.6.1 | 2014-2-14 | servedPDPPDNAddress add prefixlength | ZXUN-CG V4.13.10.P1 |
| V6.6.1 | V6.6.2 | 2014-4-11 | Add threeGPP2UserLocationInformation | ZXUN-CG V4.13.10.P2 |
| V6.6.2 | V6.6.3 | 2014-6-19 | Add chargingRuleBaseName and aFRecordInformation | ZXUN-CG V4.13.10.P3 |
| V6.6.3 | V6.6.3 | 2014-7-31 | Documentation updates | V4.13.10.P3 |
| V6.6.3 | V6.6.4 | 2014-9-24 | Correct tag value definition | V4.13.10.P4 |
| V6.6.4 | V6.7.0 | 2014-12-11 | Correct category to OPTIONAL for some fields | V4.14.10.P1 |
| V6.7.0 | V6.7.1 | 2015-4-8 | Add ePDG-CDR and tWANUserLocationInformation | V4.14.10.P2 |
| V6.7.1 | V6.7.2 | 2015-7-3 | Add listOfTrafficVolumes to PGW-CDR | V4.14.10.P3 |
| V6.7.2 | V6.7.3 | 2015-8-3 | Multiple additions to PGW-CDR | V4.14.10.P4 |
| V6.7.3 | V6.7.4 | 2016-2-23 | Add dynamicAddressFlagExt | V4.14.10.P7 |
| V6.7.4 | V6.7.5 | 2016-8-8 | Add uWANUserLocationInformation | V4.14.10.P8 |
| V6.7.5 | V6.7.6 | 2016-9-14 | Add url fields | ZXUN-CG V5.16.10 |
| V6.7.6 | V7.0.0 | 2016-11-28 | Add cPCIoTEPSOptimisationIndicator | V5.16.10.P1 |
| V7.0.0 | V7.1.0 | 2017-4-10 | Multiple additions for IoT support | V5.17.10.B2 |
| V7.1.0 | V7.1.0 | 2017-7-11 | Add sCSASAddress to PGW-CDR | V5.17.10.P1 |
| V7.1.0 | V7.1.0 | 2018-2-12 | Refine description of service condition change extensions | V5.17.10.P1 |
| V7.2.0 | V7.2.0 | 2018-2-26 | Add extended bandwidth fields | V5.18.10.B4 |
| V7.2.0 | V7.2.1 | 2018-5-17 | Add listOfRANSecondaryRATUsageReports | V5.18.10.P2.B4 |

## Table of Contents

1. [General Information](#1-general-information)
   1.1 [Abbreviation](#11-abbreviation)
   1.2 [Structure of the CDR File](#12-structure-of-the-cdr-file)
   1.3 [CDR Encoding](#13-cdr-encoding)
2. [Description of Various CDRs](#2-description-of-various-cdrs)
   2.1 [General](#21-general)
   2.2 [Field category type description](#22-field-category-type-description)
   2.3 [Record Types](#23-record-types)
   2.4 [Description of Fields](#24-description-of-fields)
3. [ASN.1 Definitions for CDR](#3-asn1-definitions-for-cdr)
   3.1 [CDR Structure](#31-cdr-structure)
   3.2 [CDR Fields Structure](#32-cdr-fields-structure)
   3.3 [TAG Values](#33-tag-values)
4. [Annex](#4-annex)

## 1. General Information

### 1.1 Abbreviation

| Abbreviation | Full Name |
|--------------|-----------|
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
[CDR1][CDR2]...[CDRn][fillers]
|--L1--|--L2--|...|--Ln--|
|---- Block size=2048/4096/8192 bytes ----|
```

#### 1.2.4 CDR File
The CDR file consists of one or multiple "Blocks" when organized by block. GSN/CGF will generate a CDR file at the specified time or the specified length according to the system configuration.

**Figure 2: Structure Diagrams of CDR File organized by block**
```
[Block 1][Block 2][Block 3]...[Block n]
|---- CDR File size ----|
```

The CDR file consists of one or multiple "CDRs" when organized by CDR. GSN/CGF will generate a CDR file at the specified time or the specified length according to the system configuration.

**Figure 3: Structure Diagrams of CDR File organized by CDR**
```
[CDR 1][CDR 2][CDR 3]...[CDR n]
|---- CDR File size ----|
```

### 1.3 CDR Encoding

#### 1.3.1 ASN.1 (Basic Encoding Rules) description

The CDRs generated at GSN/CGF is specified using the Abstract Syntax Notation One (ASN.1). ASN.1 is a language that defines the way data is sent across dissimilar communication systems. ASN.1 ensures that the data received is the same as the data transmitted by providing a common syntax for specifying application layer protocols. ASN.1 is an ISO/ITU-T standard based on the OSI model and is defined in "ASN.1 encoding rules: Specification of Basic Notation, ITU-T Recommendation X.680".

The GSN/CGF uses ASN.1 Basic Encoding Rules (BER) to encode the CDRs. BER is a set of standard rules, defined in "ASN.1 encoding rules: Specification of Basic Encoding Rules (BER), Canonical Encoding Rules (CER) and Distinguished Encoding Rules (DER) ITU-T Recommendation X.690", for encoding data types specified in ASN.1. A field containing a value of a certain data type is encoded into the following parts:
- Identifier: identifies the data type and consists of an ASN.1 tag and data structure information
- Length: the length of the content part in number of octets
- Contents: the value encoded according to data type specific rules

The ASN.1 tags assigned to CDR fields are specified in chapter 3.3.

#### 1.3.2 Encoding of the Tag

The identifier octets encode the ASN.1 tag of the data value. Two possibilities exist:

**G. Single octet encoding for tag numbers from 0 to 30 (inclusive)**

| Bits | Description |
|------|-------------|
| 8-7 | Class identifier:<br>00 = Universal<br>01 = Application<br>10 = Context-specific<br>11 = Private |
| 6 | 0 = Primitive<br>1 = Constructed |
| 5-1 | Binary integer with bit 5 as msb |

**H. Use of a leading octet for tag numbers bigger than or equal to 31**

The leading octet is encoded as follows:
- Bits 8-7: Class identifier as for single octet id
- Bit 6: 0 = Primitive, 1 = Constructed
- Bit 5-1: all bits set to 1

Subsequent octets are encoded as:
- Bit 8: set to 1 in all non-last subsequent octets
- Bits 7-1: Bits 7-1 of all subsequent octets encoded as a binary integer equal to the tag number with bit 7 of the first subsequent octet as most significant bit.

#### 1.3.3 Encoding of the length

The length octets encode the length of the following content of the data item. Three possibilities exist in ASN.1: short, long and indefinite. The indefinite variant is not used in this CDR format.

**G. Short length encoding for length from 0 to 127 (inclusive)**
- Octet 1: 0LLLLLLL where LLLLLLL represents the length of the content

**H. Long length encoding for length > 127**
- Octet 1: 1 followed by 0 < n < 127
- Octets 2 to n+1: LLLLLLLL represents the length of the content

#### 1.3.4 Encoding of the content

The numbering of bits within one octet and the encoding of a binary value in an octet structure can be found in the following:
- bit 8 of octet 1 is the most significant bit (msb)
- bit 1 of octet n is the least significant bit (lsb)

#### 1.3.5 ASN.1 BER Encoding of Integers

ASN.1 defines the integer type as a simple type with distinguished values, which are the positive and negative numbers, including zero. The content part for a field of type integer is BER encoded in one or more octets. The content octets contain the two's complement binary number equal to the integer value, and consisting of bits 8 to 1 of the first octet, followed by bit 8 to 1 of each octet in turn up to and including the last content octet. The two's complement form implies that the most significant bit of the content octets indicates the sign of the value (0 for positive numbers and 1 for negative numbers). This means that the highest positive number a sequence of X octets can represent is: 2^(8X-1)-1.

Example-1: The highest positive number represented by four octets (32 bits) is: 2^31-1.
Example-2: To represent the highest value for a 32 bits positive integer (2^32-1), five content octets are needed.

#### 1.3.6 Structure Diagrams of Charging Data Record

**Figure 4: Structure Diagrams of Charging Data Record**
```
[TAG = Charging Data Record Tag]
[Length of Charging Data Record]
[TAG = Field Tag 1]
[Length of Field 1]
[Value of Field 1]
...
[TAG = Field Tag m]
[Length of Field m]
[H'30]
[Value of Sequence 1]
[H'30]
[Value of Sequence 2]
...
[TAG = Field Tag n]
[Length of Field n]
[Value of Field n]
```

## 2. Description of Various CDRs

### 2.1 General

The SGSN shall collect the following charging information:
- Usage of the radio interface: The charging information shall provide respectively the amount of data in MO and MT directions and reflect PDP protocol type and QoS information
- Usage of PDP address: The charging information shall provide the period of usage of PDP addresses by the MS.
- Usage of the general GPRS resources: The charging information shall provide MS's usage of other GPRS-related resources (e.g. mobility management overhead)
- Location of MS: The charging information shall provide HPLMN, VPLMN, and accurate location information (e.g. RAI, CI)

The GGSN shall collect the following charging information:
- Destination and source: The charging information shall provide destination and source addresses for the PDP context
- Usage of the external data networks: The charging information shall describe the amount of data sent and received to and from the external data network
- Usage of PDP address: The charging information shall provide the period of usage of PDP addresses by the MS.
- Location of MS: The charging information shall provide HPLMN, VPLMN, and accurate location information. For GGSN, the accurate location information of MS is SGSN address.

The S-GW shall collect the following charging information:
- Usage of the radio interface: The charging information shall provide respectively the amount of data in MO and MT directions and reflect PDP protocol type and QoS information
- The charging information shall provide the duration of the IP-CAN bearer with date and time information.
- Usage of the general Packet-Switched domain resources: The charging information shall provide MS's usage of other Packet-Switched domain-related resources (e.g. mobility management overhead)
- Location of MS: The charging information shall provide HPLMN, VPLMN, and accurate location information (e.g. RAI,LAC,CI).

The P-GW shall collect the following charging information:
- Destination and source: The charging information shall provide destination and source addresses for the IP CAN bearer.
- Data volumes on both the uplink and downlink direction shall be counted separately. The data volumes shall reflect the data as delivered to and from the user.
- The charging information shall provide the duration of the IP-CAN bearer with date and time information.
- Location of MS: The charging information shall provide HPLMN, VPLMN, and accurate location information. For GGSN, the accurate location information of MS is SGSN address.
- The P-GW may be capable of identifying data volumes, elapsed time or events for individual service data flows (flow based bearer charging). One PCC rule identifies one service data flow.

### 2.2 Field category type description

| Category | Description |
|----------|-------------|
| M | This field is Mandatory and shall always be present in the CDR. |
| C | This field shall be present in the CDR only when certain Conditions are met. These Conditions are specified as part of the field definition |
| O_M | This is a field that, if provisioned by the operator to be present, shall always be included in the CDRs. In other words, an O_M parameter that is provisioned to be present is a mandatory parameter. |
| O_C | This is a field that, if provisioned by the operator to be present, shall be included in the CDRs when the required conditions are met. In other words, an O_C parameter that is configured to be present is a conditional parameter. |

### 2.3 Record Types

#### 2.3.1 S-CDR(SGSNPDPRecord)

S-CDR is used to collect charging data of MS-related IP CAN bearer in the SGSN. The trigger conditions of generating S-CDR in SGSN include: termination of the IP CAN bearer, data volume limit, time limit and changing of charging conditions up to the maximum number.

**Table 2: SGSN IP CAN bearer Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN IP CAN bearer record. |
| Network Initiated PDP Context | O_C | A flag that is present if this is a network initiated IP CAN bearer. |
| Served IMSI | O_C | IMSI of the served party. |
| Served IMEI | O_C | The IMEI of the ME, if available. |
| List of SGSN Address | O_C | The record is the current SGSN address before consolidation, and IP CAN bearer-involved IP address list of SGSN after consolidation. |
| MS Network Capability | O_C | The mobile station Network Capability. |
| Routing Area Code (RAC) | O_C | RAC at the time of "Record Opening Time". |
| Location Area Code (LAC) | O_C | LAC at the time of "Record Opening Time". |
| Cell Identifier | O_C | Cell identity for GSM or Service Area Code (SAC) for UMTS at the time of "Record Opening Time". |
| Charging ID | O_C | IP CAN bearer identifier used to identify this IP CAN bearer in different records created by PCNs. |
| GGSN Address Used | O_C | The control plane IP address of the P-GW currently used. The P-GW address is always the same for an activated IP CAN bearer. |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN). |
| PDP Type | O_C | PDP type, i.e. IP, PPP, IHOSS:OSP. |
| Served PDP Address | O_C | PDP address of the served IMSI, i.e. Ipv4 or Ipv6. This parameter shall be present except when both the PDP type is PPP and dynamic PDP address assignment is used. |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this IP CAN bearer, each change is time stamped. Charging conditions are used to categorize traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data volumes are also listed. |
| Record Opening Time | O_C | Time stamp when IP CAN bearer is activated in this SGSN or record opening time on subsequent partial records. |
| Duration | O_C | Duration of this record in the SGSN. |
| SGSN Change | O_C | Present if this is first record after SGSN change. |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN. |
| Diagnostics | O_C | A more detailed reason for the release of the connection. |
| List of Record Sequence Number | O_C | Partial record sequence number in this SGSN. Only present in case of partial records. |
| Node ID | O_C | Name of the recording entity. |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension. |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types. |
| APN Selection Mode | O_C | An index indicating how the APN was selected. |
| Access Point Name Operator Identifier | O_C | The Operator Identifier part of the APN. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber. |
| Charging Characteristics | O_C | The Charging Characteristics applied to the IP CAN bearer. |
| System Type | O_C | This field indicates the Radio Access Technology (RAT) type, e.g. UTRAN or GERAN, currently used by the Mobile Station as defined in TS 29.060 [204]. |
| CAMEL Information | O_C | Set of CAMEL information related to IP CAN bearer. For more information see Description of Record Fields. This field is present if CAMEL service is activated. |
| RNC Unsent Downlink Volume | O_C | The downlink data volume, which the RNC has not sent to MS. This field is present when the RNC has provided unsent downlink volume count at RAB release. |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected. |
| Dynamic Address Flag | O_C | Indicates whether served PDP address is dynamic, which is allocated during IP CAN bearer activation. This field is missing if address is static. |
| Consolidation Result | O_C | The consolidation result of partial record generated from one PDP context, only occurs in consolidated record. |
| List of Local Record Sequence Number | O_C | Local sequence number of the original part of record and correspondent SGSN address form a list, which occurs only in consolidated record. |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time. |

#### 2.3.6 LCS-MO-CDR(SGSNMOLCSRecord)

An SGSN Mobile originated LCS record shall be produced for each mobile originated location request is performed via the SGSN

**Table 7: SGSN Mobile Originated LCS Record (SGSN-LCS-MO)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN Mobile Originated LCS. |
| Recording Entity | O_C | The E.164 number of the SGSN. |
| LCS Client Type | O_C | The type of the LCS client that invoked the LR, if available. |
| LCS Client Identity | O_C | Further identification of the LCS client, if available. |
| Served IMSI | O_C | The IMSI of the subscriber. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber. |
| SGSN Address | O_C | The IP address of the current SGSN. |
| Location Method | O_C | The type of the location request. |
| LCS QoS | O_C | QoS of the LR, if available. |
| LCS Priority | O_C | Priority of the LR, if available |
| MLC Number | O_C | The E.164 address of the involved GMLC, if applicable. |
| Event Time stamp | O_C | The time at which the Perform_Location_Request is sent by the SGSN. |
| Measurement Duration | O_C | The duration of proceeding the location request. |
| Location | O_C | The LAC and CI when the LR is received. |
| Routing Area Code | O_C | The Routing Area Code from which the LCS originated. |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful. |
| Positioning Data | O_C | The positioning method used or attempted, if available. |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface. |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened. |
| Node ID | O_C | Name of the recording entity. |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types. |
| Charging Characteristics | O_C | The Charging Characteristics flag set used by the SGSN. |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected. |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available. |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension. |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN. |

#### 2.3.7 LCS-MT-CDR(SGSNMTLCSRecord)

An SGSN Mobile terminated LCS record shall be produced for each mobile terminated location request is performed via the SGSN.

**Table 8: SGSN Mobile Terminated LCS Record (SGSN-LCS-MT)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN Mobile Terminated LCS. |
| Recording Entity | O_C | The E.164 number of the SGSN. |
| LCS Client Type | O_C | The type of the LCS client that invoked the LR. |
| LCS Client Identity | O_C | Further identification of the LCS client. |
| Served IMSI | O_C | The IMSI of the subscriber. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber. |
| SGSN Address | O_C | The IP address of the current SGSN. |
| Location Type | O_C | The type of the estimated location. |
| LCS QoS | O_C | QoS of the LR, if available. |
| LCS Priority | O_C | Priority of the LR, if available |
| MLC Number | O_C | The E.164 address of the requesting GMLC |
| Event Time stamp | O_C | The time at which the Perform_Location_Request is sent by the SGSN. |
| Measurement Duration | O_C | The duration of proceeding the location request. |
| Notification To MS User | O_C | The privacy notification to MS user that was applicable when the LR was invoked, if available. |
| Privacy Override | O_C | This parameter indicates the override MS privacy by the LCS client, if available. |
| Location | O_C | The LAC and CI when the LR is received. |
| Routing Area Code | O_C | The Routing Area Code to which the LCS terminated. |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful. |
| Positioning Data | O_C | The positioning method used or attempted, if available. |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface. |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened. |
| Node ID | O_C | Name of the recording entity. |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types. |
| Charging Characteristics | O_C | The Charging Characteristics used by the SGSN. (always use the subscribed CC) |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected. (Only subscribed/home default/visited default) |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available. |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension. |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN. |

#### 2.3.8 LCS-NI-CDR(SGSNNILCSRecord)

An SGSN Network induced LCS record shall be produced for each network induced location request is performed via the SGSN

**Table 9: SGSN Network Induced LCS Record (SGSN-LCS-NI)**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN Network Induced LCS. |
| Recording Entity | O_C | The E.164 number of the SGSN. |
| LCS Client Type | O_C | The type of the LCS client that invoked the LR, if available. |
| LCS Client Identity | O_C | Further identification of the LCS client, if available. |
| Served IMSI | O_C | The IMSI of the subscriber if supplied. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber if supplied. |
| SGSN Address | O_C | The IP address of the current SGSN. |
| Served IMEI | O_C | The IMEI of the ME, if available. |
| LCS QoS | O_C | QoS of the LR, if available. |
| LCS Priority | O_C | Priority of the LR, if available |
| MLC Number | O_C | The E.164 address of the involved GMLC, if applicable. |
| Event Time stamp | O_C | The time at which the Perform_Location_Request is sent by the SGSN. |
| Measurement Duration | O_C | The duration of proceeding the location request. |
| Location | O_C | The LAC and CI when the LR is received. |
| Routing Area Code | O_C | The Routing Area Code from which the LCS originated. |
| Location Estimate | O_C | The location estimate for the subscriber if contained in geographic position and the LR was successful. |
| Positioning Data | O_C | The positioning method used or attempted, if available. |
| LCS Cause | O_C | The result of the LR if any failure or partial success happened as known at radio interface. |
| Diagnostics | O_C | A more detailed information about the Cause for Record Closing if any failure or partial success happened. |
| Node ID | O_C | Name of the recording entity. |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types. |
| Charging Characteristics | O_C | The Charging Characteristics flag set used by the SGSN. |
| Charging Characteristics Selection Mode | O_C | Holds information about how Charging Characteristics were selected. |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station as defined in TS 29.061, when available. |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension. |
| Cause for Record Closing | O_C | The reason for closure of the record from this SGSN. |
| Served PDP/PDN Address Extension | O_C | This field contains the Ipv4 address for the PDN connection when dual-stack Ipv4 Ipv6 is used. |

#### 2.3.2 G-CDR(GGSNPDPRecord)

G-CDR is used to collect charging data of MS-related PDP context in the GGSN. The trigger conditions of generating G-CDR in GGSN include: termination of the PDP context, data volume limit, time limit and changing of charging conditions up to the maximum number.

**Table 3: GGSN PDP Context Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | GGSN PDP context record. |
| Network initiated PDP context | O_C | A flag indicating whether this is a network initiated PDP context. |
| Served IMSI | O_C | IMSI of the served party (non-anonymous connection) |
| GGSN Address used | O_C | The control plane IP address of the GGSN used. |
| Charging ID | O_C | PDP context identifier used to identify this PDP context in different records created by GSNs |
| SGSN Address | O_C | List of SGSN addresses used during this record. |
| Access Point Name Network Identifier | O_C | The logical name of the connected access point to the external packet data network (network identifier part of APN) |
| PDP Type | O_C | PDP type, i.e. IP, PPP, IHOSS:OSP |
| Served PDP Address | O_C | PDP address, i.e. Ipv4 or Ipv6. This parameter shall be present except when both the PDP type is PPP and dynamic PDP address assignment is used. |
| Dynamic Address Flag | O_C | Indicates whether served PDP address is dynamic, which is allocated during PDP context activation. This field is missing if address is static. |
| List of Traffic Data Volumes | O_C | A list of changes in charging conditions for this PDP context, each change is time stamped. Charging conditions are used to categorise traffic volumes, such as per tariff period. Initial and subsequently changed QoS and corresponding data values are listed. |
| Record Opening Time | O_C | Time stamp when PDP context is activated in this GGSN or record opening time on subsequent partial records. |
| Duration | O_C | Duration of this record in the CDR |
| Cause for Record Closing | O_C | Reason for closure of the record |
| Diagnostics | O_C | A more detailed reason for the release of the connection. |
| List of Record Sequence Number | O_C | For those records that are partial record S/Ns generated in GGSN before consolidation, and the S/N list of the original partial record after consolidation, the list includes GGSN addresses. |
| Node ID | O_C | Name of the recording entity |
| Record Extensions | O_C | Supplementary field (used for content charging) |
| Local Sequence Number | M | This node creates S/N of all CDR types |
| APN Selection Mode | O_C | An index indicating how the APN was selected. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber. |
| Charging Characteristics | O_C | The Charging Characteristics applied to the PDP context. |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected. |
| SGSN PLMN Identifier | O_C | SGSN PLMN Identifier (MCC and MNC) used during this record. |
| Consolidation Result | O_C | The consolidation result of partial record generated from one PDP context, only occurs in consolidated record. |
| List of Local Record Sequence Number | O_C | Local sequence number of the original partial record and correspondent GGSN address form a list, which occurs only in consolidated record. |
| RAT Type | O_C | This field indicates the Radio Access Technology (RAT) type currently used by the Mobile Station.The field is present in the G-CDR if provided by SGSN. |
| IMS Signalling Context | O_C | Included if the IM-CN Subsystem Signalling Flag is set, Which indicate PDP context is used for IMS signaling. |
| External Charging Identifier | O_C | Holds a Charging Identifier and is present only if it is received from a non-GPRS, external network entity |
| User Location Information | O_C | This field contains the User Location Information of the MS, if provided by SGSN. |
| Served IMEISV | O_C | IMEISV of the ME, if available. |
| MS Time Zone | O_C | This field contains the MS Time Zone the MS is currently located as defined in TS 29.060, if provided by SGSN. |
| URL | O_C | This field indicates the first visited URL of the subscriber. |
| Roaming Indicator | O_C | This field indicates the roaming CDR |
| DiameterSessionID | O_C | This field is used to associate G-CDR with CDR generated by OCS, this field is also used to judge whether the flag of OCS is inuse. |
| Served PDP/PDN Address Extension | O_C | This field contains the Ipv4 address for the PDN connection when dual-stack Ipv4 Ipv6 is used. |
| IMSI Unauthenticated Flag | O_C | This field indicates the provided served IMSI is not authenticated (emergency bearer service situation). |
| PS Furnish Charging Information | O_C | This field contains charging information in case it is sent by OCS. |
| EPC User Location Information | O_C | This field contains the User Location Information of the MS for EPC case if available as defined in 29.274 [210]. |
| EPC Qos Information | O_C | Contains the QoS applied for IP CAN bearer for EPC case. |

#### 2.3.3 M-CDR(SGSNMMRecord)

M-CDR is used to collect charging data of MS-related mobility management in the SGSN. M-CDR is collected when MS starts attachment, until mobile subscriber detach.

**Table 4: SGSN Mobile Station Mobility Management Data**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | SGSN mobility management record. |
| Served IMSI | O_C | IMSI of the MS. |
| Served IMEI | O_C | The IMEI of the ME, if available. |
| SGSN Address | O_C | The IP address of the current SGSN. |
| MS Network Capability | O_C | The mobile station network capability. |
| Routing Area | O_C | Routing Area at the time of the Record Opening Time. |
| Local Area Code | O_C | Location Area Code at the time of Record Opening Time. |
| Cell Identifier | O_C | Cell identity or Service Area Code (SAC) at the time of "Record Opening Time". |
| Change of Location | O_C | A list of changes in Routing Area Code, each with a time stamp. This field is not required if partial records are generated when the location changes. |
| Record Opening Time | O_C | Timestamp when MS is attached to this SGSN or record opening time on following partial record. |
| Duration | O_C | Duration of this record. |
| SGSN Change | O_C | Present if this is first record after SGSN change. |
| Cause for Record Closing | O_C | The reason for the closure of the record in this SGSN. |
| Diagnostics | O_C | A more detailed reason for the release of the connection. |
| Record Sequence Number | O_C | Partial record sequence number in this SGSN; only present in case of partial records. |
| Node ID | O_C | Name of the recording entity. |
| Record Extensions | O_C | A set of network operator/manufacturer specific extensions to the record. Conditioned upon the existence of an extension. |
| Local Sequence Number | M | Consecutive record number created by this node. The number is allocated sequentially including all CDR types. |
| Served MSISDN | O_C | The primary MSISDN of the subscriber. |
| Charging Characteristics | O_C | The Charging Characteristics used by the SGSN. |
| CAMEL Information | O_C | Set of CAMEL information related to Attach/Detach session. For more information see Description of Record Fields. This field is present if CAMEL service is activated. |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface. |
| Charging Characteristics selection mode | O_C | Holds information about how Charging Characteristics were selected. |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time. |

#### 2.3.4 S-SMO-CDR(SGSNSMORecord)

S-SMO-CDR shall be produced for each short message sent by an MS through the SGSN.

**Table 5: SGSN Mobile Originated SMS Record**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | The CDR is S-SMO-CDR. |
| Served IMSI | O_C | The IMEI of the PS subscriber. |
| Served IMEI | O_C | The IMEI of the PS subscriber. |
| Served MSISDN | O_C | The MSISDN of the PS subscriber. |
| MS Network Capability | O_C | The mobile station network capability. |
| Service Centre | O_C | The address (E.164) of the SMS-service centre. |
| Recording Entity | O_C | The E.164 number of the SGSN. |
| Location Area Code | O_C | The Location Area Code from which the message originated. |
| Routing Area Code | O_C | The Routing Area Code from which the message originated. |
| Cell Identifier | O_C | The Cell or Service Area Code (SAC) from which the message originated. |
| Message Reference | O_C | Identifier of the short message. |
| Event Time Stamp | O_C | The time at which the message was received by the SGSN from the subscriber. |
| SMS Result | O_C | The result of the attempted delivery if unsuccessful. |
| Record Extensions | O_C | Supplementary field. |
| Node ID | O_C | Code of SGSN. |
| Local Sequence Number | M | This node creates S/N of all CDR types. |
| Charging Characteristics | O_C | Flag of charging Characteristics in subscriber data. |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface. |
| Destination Number | O_C | The destination short message subscriber number. |
| CAMEL Information | O_C | Set of CAMEL information related to SMS. |
| Charging Characteristics selection mode | O_C | Indicates selection mode of charging characteristics. |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time. |

#### 2.3.5 S-SMT-CDR(SGSNSMTRecord)

S-SMT-CDR shall be produced for each short message received by an MS through the SGSN.

**Table 6: SGSN Mobile Received SMS Record**

| Field | Attr. | Description |
|-------|-------|-------------|
| Record Type | M | The CDR is S-SMT-CDR |
| Served IMSI | O_C | The IMEI of the PS subscriber |
| Served IMEI | O_C | The IMEI of the PS subscriber |
| Served MSISDN | O_C | The MSISDN of the PS subscriber. |
| MS Network Capability | O_C | The mobile station network capability |
| Service Centre | O_C | The address (E.164) of the SMS-service centre. |
| Recording Entity | O_C | The E.164 number of the SGSN. |
| Location Area Code | O_C | The location area code from which the message is recieved. |
| Routing Area Code | O_C | The routing area code from which the message is recieved. |
| Cell Identifier | O_C | The Cell or Service Area Code (SAC) from which the message is recieved. |
| Event Time Stamp | O_C | The time at which the message was received by the SGSN from the subscriber. |
| SMS Result | O_C | The result of the attempted delivery if unsuccessful. |
| Record Extensions | O_C | Supplementary field |
| Node ID | O_C | Code of SGSN |
| Local Sequence Number | M | This node creates S/N of all CDR types |
| Charging Characteristics | O_C | Flag of charging characteristics in subscriber data. |
| System Type | O_C | Indicates the type of air interface used. This field is present when UTRAN or GERAN air-interface is used; It is omitted when the service is provided by a GSM air interface. |
| CAMEL Information | O_C | Set of CAMEL information related to SMS. |
| Charging Characteristics selection mode | O_C | Indicates selection mode of charging characteristics. |
| Origination Number | O_C | The origination short message subscriber number. |
| Cell PLMN Identifier | O_C | The MCC and MNC of the Cell at the time of Record Opening Time. |
