# \DefaultApi

All URIs are relative to *https://localhost*

Method | HTTP request | Description
------------- | ------------- | -------------
[**Healthy**](DefaultApi.md#Healthy) | **Get** /healthy | 
[**Info**](DefaultApi.md#Info) | **Get** / | 
[**Openapi**](DefaultApi.md#Openapi) | **Get** /openapi | 
[**Ready**](DefaultApi.md#Ready) | **Get** /ready | 


# **Healthy**
> Healthy(ctx, )


Check if the bridge is running. This does not necessarily imply that it is ready to accept requests.

### Required Parameters
This endpoint does not need any parameter.

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Info**
> BridgeInfo Info(ctx, )


Retrieves information about the Kafka Bridge instance, in JSON format.

### Required Parameters
This endpoint does not need any parameter.

### Return type

[**BridgeInfo**](BridgeInfo.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Openapi**
> string Openapi(ctx, )


Retrieves the OpenAPI v2 specification in JSON format.

### Required Parameters
This endpoint does not need any parameter.

### Return type

**string**

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

# **Ready**
> Ready(ctx, )


Check if the bridge is ready and can accept requests.

### Required Parameters
This endpoint does not need any parameter.

### Return type

 (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

[[Back to top]](#) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to Model list]](../README.md#documentation-for-models) [[Back to README]](../README.md)

