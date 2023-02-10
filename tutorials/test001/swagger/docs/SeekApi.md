# \SeekApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**Seek**](SeekApi.md#Seek) | **Post** /consumers/{groupid}/instances/{name}/positions | 
[**SeekToBeginning**](SeekApi.md#SeekToBeginning) | **Post** /consumers/{groupid}/instances/{name}/positions/beginning | 
[**SeekToEnd**](SeekApi.md#SeekToEnd) | **Post** /consumers/{groupid}/instances/{name}/positions/end | 


# **Seek**
> Seek(ctx, groupid, name, body)


Configures a subscribed consumer to fetch offsets from a particular offset the next time it fetches a set of records from a given topic partition. This overrides the default fetch behavior for consumers. You can specify one or more topic partitions.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the consumer belongs. | 
  **name** | **string**| Name of the subscribed consumer. | 
  **body** | [**OffsetCommitSeekList**](OffsetCommitSeekList.md)| List of partition offsets from which the subscribed consumer will next fetch records. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **SeekToBeginning**
> SeekToBeginning(ctx, groupid, name, body)


Configures a subscribed consumer to seek (and subsequently read from) the first offset in one or more given topic partitions.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the subscribed consumer. | 
  **body** | [**Partitions**](Partitions.md)| List of topic partitions to which the consumer is subscribed. The consumer will seek the first offset in the specified partitions. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **SeekToEnd**
> SeekToEnd(ctx, groupid, name, optional)


Configures a subscribed consumer to seek (and subsequently read from) the offset at the end of one or more of the given topic partitions.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the subscribed consumer. | 
 **optional** | ***SeekToEndOpts** | optional parameters | nil if no parameters

### Optional Parameters
Optional parameters are passed through a pointer to a SeekToEndOpts struct

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------


 **body** | [**optional.Interface of Partitions**](Partitions.md)| List of topic partitions to which the consumer is subscribed. The consumer will seek the last offset in the specified partitions. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

