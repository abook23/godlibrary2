## Retrofit2

### 初始化
```java
//appliaction 中初始化
ApiService.init(getApplicationContext(),String baseUrl)

```

### 创建API
```java
public interface UserApi {

    @POST("member/login")
    @FormUrlEncoded
    Observable<Response<UserInfo>> login(@Query("mobile") String userName, @Query("passWord") String password, @Query("type") String type);

    Call<Response<UserInfo>> login(@Query("mobile") String userName, @Query("passWord") String password);

    @GET("member/getMemberInfo")
    Observable<Response<UserInfo>> userInfo();
}
```

### 请求数据
```java
    private void login() {
        ApiService.create(UserApi.class).login("userName", "password", "1")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebObserver<Response<UserInfo>>() {
                    @Override
                    protected void onSuccess(Response<UserInfo> userInfoResponse) {
                        Toast.makeText(getApplicationContext(), "请求成功" + userInfoResponse.getData().getID(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

    public void getUserInfo() {
        ApiService.create(UserApi.class).userInfo()
                .compose(RxJavaUtils.<Response<UserInfo>>defaultSchedulers())// 等于 .subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new WebObserver<Response<UserInfo>>() {
                    @Override
                    protected void onSuccess(Response<UserInfo> userInfoResponse) {
                        Toast.makeText(getApplicationContext(), "请求成功" + userInfoResponse.getState(), Toast.LENGTH_SHORT).show();
                    }
                });
    }
```

### 高级功能

#### 网络请求 全局拦截监听
用于以下场景
* 登录超时 401
* 或者服务器自定义返回
```java
ApiService apiService = ApiService.init(getApplicationContext(), BASE_URL);
//apiService.setTimeOut(30000, 30000);
apiService.addInterceptor(new TokenInterceptor<RootBean<UserInfo>>() {
    @Override
    protected boolean onAuthenticator(Response response) {
        return response.code() == 401;//401 需要重新登录
    }

    @Override
    protected Call<RootBean<UserInfo>> onAfresh() {//重新登录接口
        String userId = PreferenceUtils.getParam(SpfKey.userId);
        UserDao userDao = BaseApplication.getDaoSession().getUserDao();
        User user = userDao.load(userId);
        if (user == null) {
            ActivityManager2.User.login();
        }
        return ApiService.create(UserApi.class).loginAfresh(user.getUsername(), user.getPassword(), user.getType());
    }

    @Override
    protected Request onNewRequest(Request OldRequest, RootBean<UserInfo> userInfoRootBean) {
        //验证登录成功
        //可以修改再次访问的接口内容,不然 token
        return null;
    }
});
```
UserApi--> loginAfresh
```java
    /**
     * 重新登录
     *
     * @param userName 用户名
     * @param password 密码
     * @param type     类型 1：司机；2：货主
     */
    @POST(ServiceURL.login)
    @FormUrlEncoded
    Call<RootBean<UserInfo>> loginAfresh(
            @Field("mobile") String userName,
            @Field("passWord") String password,
            @Field("type") int type);
```
