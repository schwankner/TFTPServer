# TFTPServer
Server for the implementaion of the TFTP speziification [RFC 1350](https://tools.ietf.org/html/rfc1350).
## Usage     
| Parameter       | Description                                              | Default | Example                       |
| :--------------- |:--------------------------------------------------------| :-------:|------------------------------:|
| -p, --remotePort | remotePort for connection with remote host.             | 69      | 65054 |
| -r, --retries    | How many times tftpclient retries to send its messages. | 5 sec.  | 3 |
| -t, --timeout    | timeout between sending and retries.                    | 10 sec. | 0 |
| -v, --verbose    | verbose output for debugging                            |         |   |

>--remotePort=69 --retries=5 --timeout=10 --verbose
