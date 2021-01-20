package com.spldeolin.allison1875.docanalyzer.yapi;

import java.util.List;
import java.util.Map;
import com.fasterxml.jackson.databind.JsonNode;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.CommonRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.InterfaceListMenuRespDto;
import com.spldeolin.allison1875.docanalyzer.yapi.javabean.ProjectGetRespDto;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;

/**
 * @author Deolin 2021-01-01
 */
public interface YapiApi {

    @GET("/api/project/get")
    Call<CommonRespDto<ProjectGetRespDto>> getProject(@Query("token") String token);

    @GET("/api/interface/list_menu")
    Call<CommonRespDto<List<InterfaceListMenuRespDto>>> listCats(@Query("token") String token,
            @Query("project_id") Long projectId);

    @GET("/api/interface/list_menu")
    Call<JsonNode> listCatsAsJsonNode(@Query("token") String token, @Query("project_id") Long projectId);

    @FormUrlEncoded
    @POST("/api/interface/add_cat")
    Call<Object> createCat(@Field("desc") String desc, @Field("name") String name, @Field("project_id") Long projectId,
            @Field("token") String token);

    @GET("/api/interface/get")
    Call<JsonNode> getEndpoint(@Query("id") Long id, @Query("token") String token);

    @POST("/api/interface/save")
    Call<Object> createOrUpdateEndpoint(@Body Map<String, Object> form);

    @POST("/api/interface/up")
    Call<Object> updateEndpoint(@Body Map<String, Object> body);

}
