package com.atguigu.gmall.constutil;

public interface JedisConst {
    //sku拼接前缀
    public static final String SKU_PREFIX="sku:" ;
    //sku拼接后缀
     public static final String SKU_SUFFIX=":info";
     //过期时间
      public static final int TIME_OUT=24*60*60;
      //sku拼接锁后缀
     public static final String SKULOCK_SUFFIX=":lock";
     //lock锁过期时间
     public static final int SKULOCK_EXPIRE_PX=10*1000;

}
