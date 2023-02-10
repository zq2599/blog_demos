# \TopicsApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**GetOffsets**](TopicsApi.md#GetOffsets) | **Get** /topics/{topicname}/partitions/{partitionid}/offsets | 
[**GetPartition**](TopicsApi.md#GetPartition) | **Get** /topics/{topicname}/partitions/{partitionid} | 
[**GetTopic**](TopicsApi.md#GetTopic) | **Get** /topics/{topicname} | 
[**ListPartitions**](TopicsApi.md#ListPartitions) | **Get** /topics/{topicname}/partitions | 
[**ListTopics**](TopicsApi.md#ListTopics) | **Get** /topics | 
[**Send**](TopicsApi.md#Send) | **Post** /topics/{topicname} | 
[**SendToPartition**](TopicsApi.md#SendToPartition) | **Post** /topics/{topicname}/partitions/{partitionid} | 


# **GetOffsets**
> OffsetsSummary GetOffsets(ctx, topicname, partitionid)


Retrieves a summary of the offsets for the topic partition.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic containing the partition. | 
  **partitionid** | **int32**| ID of the partition. | 

### Return type

[**OffsetsSummary**](OffsetsSummary.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **GetPartition**
> PartitionMetadata GetPartition(ctx, topicname, partitionid)


Retrieves partition metadata for the topic partition.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic to send records to or retrieve metadata from. | 
  **partitionid** | **int32**| ID of the partition to send records to or retrieve metadata from. | 

### Return type

[**PartitionMetadata**](PartitionMetadata.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **GetTopic**
> TopicMetadata GetTopic(ctx, topicname)


Retrieves the metadata about a given topic.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic to send records to or retrieve metadata from. | 

### Return type

[**TopicMetadata**](TopicMetadata.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **ListPartitions**
> []PartitionMetadata ListPartitions(ctx, topicname)


Retrieves a list of partitions for the topic.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic to send records to or retrieve metadata from. | 

### Return type

[**[]PartitionMetadata**](PartitionMetadata.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **ListTopics**
> []string ListTopics(ctx, )


Retrieves a list of all topics.

### Required Parameters
This endpoint does not need any parameter.

### Return type

**[]string**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Send**
> OffsetRecordSentList Send(ctx, topicname, body, optional)


Sends one or more records to a given topic, optionally specifying a partition, key, or both.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic to send records to or retrieve metadata from. | 
  **body** | [**ProducerRecordList**](ProducerRecordList.md)|  | 
 **optional** | ***SendOpts** | optional parameters | nil if no parameters

### Optional Parameters
Optional parameters are passed through a pointer to a SendOpts struct

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------


 **async** | **optional.Bool**| Whether to return immediately upon sending records, instead of waiting for metadata. No offsets will be returned if specified. Defaults to false. | 

### Return type

[**OffsetRecordSentList**](OffsetRecordSentList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.json.v2+json, application/vnd.kafka.binary.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **SendToPartition**
> OffsetRecordSentList SendToPartition(ctx, topicname, partitionid, body)


Sends one or more records to a given topic partition, optionally specifying a key.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **topicname** | **string**| Name of the topic to send records to or retrieve metadata from. | 
  **partitionid** | **int32**| ID of the partition to send records to or retrieve metadata from. | 
  **body** | [**ProducerRecordToPartitionList**](ProducerRecordToPartitionList.md)| List of records to send to a given topic partition, including a value (required) and a key (optional). | 

### Return type

[**OffsetRecordSentList**](OffsetRecordSentList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.json.v2+json, application/vnd.kafka.binary.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

