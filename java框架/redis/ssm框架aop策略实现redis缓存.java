http://www.tuicool.com/articles/jIVf6br
http://www.cnblogs.com/cmyxn/p/5990974.html

作用就是在每次查询接口的时候首先判断 Redis 中是否有缓存，有的话就读取，没有就查询数据库并保存到 Redis 中，下次再查询的话就会直接从缓存中读取了。

