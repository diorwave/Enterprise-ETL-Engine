
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
