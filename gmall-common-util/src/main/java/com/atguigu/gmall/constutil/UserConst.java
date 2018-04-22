package com.atguigu.gmall.constutil;

public interface UserConst {
    //用户前缀
     public static final String USER_PREFIX ="user:";
     //用户后缀
      public static final String USER_SUFFIX=":info";
      //设置用户存放redis中有效过期时间
     public static final int TIME_OUT=3*60*60;
}
