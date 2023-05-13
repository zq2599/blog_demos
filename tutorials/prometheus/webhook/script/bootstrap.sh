
#!/bin/bash
CURDIR=$(cd $(dirname $0); pwd)
BinaryName=webhook
echo "$CURDIR/bin/${BinaryName}"
exec $CURDIR/bin/${BinaryName}
