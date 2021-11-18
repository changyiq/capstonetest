# ImageApi

All URIs are relative to */*

Method | HTTP request | Description
------------- | ------------- | -------------
[**apiImageFilenameGet**](ImageApi.md#apiImageFilenameGet) | **GET** /api/Image/{filename} | 
[**apiImagePost**](ImageApi.md#apiImagePost) | **POST** /api/Image | 

<a name="apiImageFilenameGet"></a>
# **apiImageFilenameGet**
> apiImageFilenameGet(filename)



### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = ImageApi()
val filename : kotlin.String = filename_example // kotlin.String | 
try {
    apiInstance.apiImageFilenameGet(filename)
} catch (e: ClientException) {
    println("4xx response calling ImageApi#apiImageFilenameGet")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ImageApi#apiImageFilenameGet")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **filename** | **kotlin.String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: Not defined

<a name="apiImagePost"></a>
# **apiImagePost**
> apiImagePost(imageSource, imageFile)



### Example
```kotlin
// Import classes:
//import io.swagger.client.infrastructure.*
//import io.swagger.client.models.*;

val apiInstance = ImageApi()
val imageSource : kotlin.String = imageSource_example // kotlin.String | 
val imageFile : kotlin.Array<kotlin.Byte> = imageFile_example // kotlin.Array<kotlin.Byte> | 
try {
    apiInstance.apiImagePost(imageSource, imageFile)
} catch (e: ClientException) {
    println("4xx response calling ImageApi#apiImagePost")
    e.printStackTrace()
} catch (e: ServerException) {
    println("5xx response calling ImageApi#apiImagePost")
    e.printStackTrace()
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **imageSource** | **kotlin.String**|  | [optional]
 **imageFile** | **kotlin.Array&lt;kotlin.Byte&gt;**|  | [optional]

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: multipart/form-data
 - **Accept**: Not defined

