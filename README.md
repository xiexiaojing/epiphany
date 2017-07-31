# epiphany
数据推送框架

1>定时清理磁盘，按照策略分配线程，将全量数据多线程写入磁盘
2>将增量数据以秒为单位推送到AMQP
3>手动补发数据


D:\xiexiaoing>mvn deploy -DlocalRepositoryPath=/com/brmayi/repository -DcreateChecksum=true

cd /com/brmayi
git add .
git commit -m "注释"
git push origin master