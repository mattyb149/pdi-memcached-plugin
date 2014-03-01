@Grab('net.spy:spymemcached:2.10.5')
import net.spy.memcached.MemcachedClient

def memcachedClient = new MemcachedClient( new InetSocketAddress('127.0.0.1', 11211 ) );

memcachedClient.set('myKey',3600, "Hello world!")
memcachedClient.set('intKey',3600, 45)
