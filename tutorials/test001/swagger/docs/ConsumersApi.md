# \ConsumersApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**Assign**](ConsumersApi.md#Assign) | **Post** /consumers/{groupid}/instances/{name}/assignments | 
[**Commit**](ConsumersApi.md#Commit) | **Post** /consumers/{groupid}/instances/{name}/offsets | 
[**CreateConsumer**](ConsumersApi.md#CreateConsumer) | **Post** /consumers/{groupid} | 
[**DeleteConsumer**](ConsumersApi.md#DeleteConsumer) | **Delete** /consumers/{groupid}/instances/{name} | 
[**ListSubscriptions**](ConsumersApi.md#ListSubscriptions) | **Get** /consumers/{groupid}/instances/{name}/subscription | 
[**Poll**](ConsumersApi.md#Poll) | **Get** /consumers/{groupid}/instances/{name}/records | 
[**Seek**](ConsumersApi.md#Seek) | **Post** /consumers/{groupid}/instances/{name}/positions | 
[**SeekToBeginning**](ConsumersApi.md#SeekToBeginning) | **Post** /consumers/{groupid}/instances/{name}/positions/beginning | 
[**SeekToEnd**](ConsumersApi.md#SeekToEnd) | **Post** /consumers/{groupid}/instances/{name}/positions/end | 
[**Subscribe**](ConsumersApi.md#Subscribe) | **Post** /consumers/{groupid}/instances/{name}/subscription | 
[**Unsubscribe**](ConsumersApi.md#Unsubscribe) | **Delete** /consumers/{groupid}/instances/{name}/subscription | 


# **Assign**
> Assign(ctx, groupid, name, body)


Assigns one or more topic partitions to a consumer.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the consumer belongs. | 
  **name** | **string**| Name of the consumer to assign topic partitions to. | 
  **body** | [**Partitions**](Partitions.md)| List of topic partitions to assign to the consumer. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Commit**
> Commit(ctx, groupid, name, optional)


Commits a list of consumer offsets. To commit offsets for all records fetched by the consumer, leave the request body empty.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the consumer belongs. | 
  **name** | **string**| Name of the consumer. | 
 **optional** | ***CommitOpts** | optional parameters | nil if no parameters

### Optional Parameters
Optional parameters are passed through a pointer to a CommitOpts struct

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------


 **body** | [**optional.Interface of OffsetCommitSeekList**](OffsetCommitSeekList.md)| List of consumer offsets to commit to the consumer offsets commit log. You can specify one or more topic partitions to commit offsets for. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **CreateConsumer**
> CreatedConsumer CreateConsumer(ctx, groupid, body)


Creates a consumer instance in the given consumer group. You can optionally specify a consumer name and supported configuration options. It returns a base URI which must be used to construct URLs for subsequent requests against this consumer instance.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group in which to create the consumer. | 
  **body** | [**Consumer**](Consumer.md)| Name and configuration of the consumer. The name is unique within the scope of the consumer group. If a name is not specified, a randomly generated name is assigned. All parameters are optional. The supported configuration options are shown in the following example. | 

### Return type

[**CreatedConsumer**](CreatedConsumer.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **DeleteConsumer**
> DeleteConsumer(ctx, groupid, name)


Deletes a specified consumer instance. The request for this operation MUST use the base URL (including the host and port) returned in the response from the `POST` request to `/consumers/{groupid}` that was used to create this consumer.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the consumer belongs. | 
  **name** | **string**| Name of the consumer to delete. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **ListSubscriptions**
> SubscribedTopicList ListSubscriptions(ctx, groupid, name)


Retrieves a list of the topics to which the consumer is subscribed.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the subscribed consumer. | 

### Return type

[**SubscribedTopicList**](SubscribedTopicList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Poll**
> ConsumerRecordList Poll(ctx, groupid, name, optional)


Retrieves records for a subscribed consumer, including message values, topics, and partitions. The request for this operation MUST use the base URL (including the host and port) returned in the response from the `POST` request to `/consumers/{groupid}` that was used to create this consumer.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the subscribed consumer to retrieve records from. | 
 **optional** | ***PollOpts** | optional parameters | nil if no parameters

### Optional Parameters
Optional parameters are passed through a pointer to a PollOpts struct

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------


 **timeout** | **optional.Int32**| The maximum amount of time, in milliseconds, that the HTTP Bridge spends retrieving records before timing out the request. | 
 **maxBytes** | **optional.Int32**| The maximum size, in bytes, of unencoded keys and values that can be included in the response. Otherwise, an error response with code 422 is returned. | 

### Return type

[**ConsumerRecordList**](ConsumerRecordList.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/vnd.kafka.json.v2+json, application/vnd.kafka.binary.v2+json, application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

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

# **Subscribe**
> Subscribe(ctx, body, groupid, name)


Subscribes a consumer to one or more topics. You can describe the topics to which the consumer will subscribe in a list (of `Topics` type) or as a `topic_pattern` field. Each call replaces the subscriptions for the subscriber.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **body** | [**Topics**](Topics.md)| List of topics to which the consumer will subscribe. | 
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the consumer to subscribe to topics. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/vnd.kafka.v2+json
 - **Accept**: application/vnd.kafka.v2+json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Unsubscribe**
> Unsubscribe(ctx, groupid, name)


Unsubscribes a consumer from all topics.

### Required Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **ctx** | **context.Context** | context for authentication, logging, cancellation, deadlines, tracing, etc.
  **groupid** | **string**| ID of the consumer group to which the subscribed consumer belongs. | 
  **name** | **string**| Name of the consumer to unsubscribe from topics. | 

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

