Zapush
======

Why another zabbix - jvm bridge?
--------------------------------

Zapush aims provides a simple way to monitor java processes with [Zabbix](http://www.zabbix.com). Java processes
already expose many interesting stats using the Java Management Extensions (JMX) and it's relatively
easy to expose more custom status using JMX. However the "java" way of
obtaining these stats remotely requires use of RMI
[which is typically painful to get through firewalls](http://java.sun.com/developer/onlineTraining/rmi/RMI.html#FirewallIssues).

The excellent [Zapcat](http://www.kjkoster.org/zapcat/Zapcat_JMX_Zabbix_Bridge.html) JMX Zabbix Bridge attempts
to address this by implementing a zabbix agent
emulator inside the java application itself. This works well in many cases, but didn't meet our needs:

* as each instance of zapcat is its own agent, it shows up as a different host in zabbix. This is undesirable.
* zapcat only automatically supports passive polling and we wanted active agents to reduce the load on the zabbix sever.
* zapcat supports trapping but only by adding
[specific instructions to the monitored application](http://www.kjkoster.org/zapcat/How_To_Push.html),
and uses an old version of the zabbix agent protocol that requires a new socket connection for each stat reported

After writing zapush we discovered that zabbix 2.0
[will include a jxm proxy](http://www.zabbix.com/documentation/2.0/manual/jmx_monitoring). The source code for
this is not available at time of writing so it's difficult to be sure but it is possible that this will make
Zapush redundant.

Another interesting tool discovered after writing zapush is [jmxtrans](http://code.google.com/p/jmxtrans/)
which does a lot of the jmx parsing that zapush does, with the intent of pushing that to multiple destinations.
This looks like a really promising approach.

What is Zapush?
---------------

Zapush:

* is packaged as a war: drop it into a java servlet container, set some system properties, and it pushes all jmx stats from
 that container to zabbix
* requires zabbix 1.8 or above (it uses the json trap protocol that allows all stats to be sent in a single round trip)
* does not support apps not running in a servlet container


Get Started
-----------

* download a jetty 7 distribution (there's nothing jetty-specific in zapush, but this is the easiest way to try out)
* drop zapush.war into the webapps directory
* start jetty with: `java -Dzabbix.server=<your_zabbix_server_or_proxy_hostname>`
* go to <http://localhost:8080/zapush>

You should see something like:

    Here are the mbeans:

    java[zapush:JMImplementation:type=MBeanServerDelegate:ImplementationName] (java.lang.String) = "JMX"
    java[zapush:JMImplementation:type=MBeanServerDelegate:ImplementationVendor] (java.lang.String) = "Sun Microsystems"
    java[zapush:JMImplementation:type=MBeanServerDelegate:ImplementationVersion] (java.lang.String) = "1.6.0_16-b01"
    java[zapush:JMImplementation:type=MBeanServerDelegate:MBeanServerId] (java.lang.String) = "gnm40836_1301595946620"

Each line starts with the name reported to zabbix where:

* `java` is hardcoded
* `zapush` is the default value for the appname, override with the `zabbix.appname` system property
* `JMImplementation:type=MBeanServerDelegate:MBeanServerId` is the gorgeous naming scheme for jmx mbeans

The bit in brackets is the type of the item reported by jmx, for your information only.

The value in quotes is the value that will be reported to zabbix.

Next, in zabbix, set up items to track values for this hostname:

* set the key to the bit in bold on the web page (e.g. `java[zapush:JMImplementation:type=MBeanServerDelegate:MBeanServerId]`)
* set the type to "Zabbix trapper"
* and fill in the rest as you wish

Zapush logs to `logs/zapush.log`. You should see an entry like:

    2011-04-05 15:01:08,852 [Zabbix Trapper] INFO  zapush.Sender - success: Processed 1 Failed 200 Total 201 Seconds spent 0.003502

Zapush sent all 201 jmx items (it always sends everything), of which zabbix was able to process 1 successfully, i.e.
only one matching item had been set up in zabbix.

Settings
--------

Zapush uses system properties for configuration:

<table>
    <tr>
        <td>zabbix.server</td>
        <td>The hostname of the zabbix server or proxy to send data to (mandatory, no default)</td>
    </tr><tr>
        <td>zabbix.server.port</td>
        <td>The port to talking to on the zabbix server or proxy (default: 10051)</td>
    </tr><tr>
        <td>zabbix.hostname</td>
        <td>The name that zabbix knows this host by (default: java's best effort discovery of the local hostname)</td>
    </tr><tr>
        <td>zabbix.appname</td>
        <td>String to include at the start of each key, used when running multiple containers on the same machine (default: zapush)</td>
    </tr><tr>
        <td>zabbix.push.interval.secs</td>
        <td>How often to send a trap to zabbix (default: 60)</td>
    </tr>
</table>

