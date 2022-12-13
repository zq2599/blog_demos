# Consumer

## Properties
Name | Type | Description | Notes
------------ | ------------- | ------------- | -------------
**Name** | **string** | The unique name for the consumer instance. The name is unique within the scope of the consumer group. The name is used in URLs. If a name is not specified, a randomly generated name is assigned. | [optional] [default to null]
**Format** | **string** | The allowable message format for the consumer, which can be &#x60;binary&#x60; (default) or &#x60;json&#x60;. The messages are converted into a JSON format.  | [optional] [default to null]
**AutoOffsetReset** | **string** | Resets the offset position for the consumer. If set to &#x60;latest&#x60; (default), messages are read from the latest offset. If set to &#x60;earliest&#x60;, messages are read from the first offset. | [optional] [default to null]
**FetchMinBytes** | **int32** | Sets the minimum amount of data, in bytes, for the consumer to receive. The broker waits until the data to send exceeds this amount. Default is &#x60;1&#x60; byte. | [optional] [default to null]
**ConsumerRequestTimeoutMs** | **int32** | Sets the maximum amount of time, in milliseconds, for the consumer to wait for messages for a request. If the timeout period is reached without a response, an error is returned. Default is &#x60;30000&#x60; (30 seconds). | [optional] [default to null]
**EnableAutoCommit** | **bool** | If set to &#x60;true&#x60; (default), message offsets are committed automatically for the consumer. If set to &#x60;false&#x60;, message offsets must be committed manually. | [optional] [default to null]
**IsolationLevel** | **string** | If set to &#x60;read_uncommitted&#x60; (default), all transaction records are retrieved, indpendent of any transaction outcome. If set to &#x60;read_committed&#x60;, the records from committed transactions are retrieved. | [optional] [default to null]

[[Back to Model list]](../README.md#documentation-for-models) [[Back to API list]](../README.md#documentation-for-api-endpoints) [[Back to README]](../README.md)


