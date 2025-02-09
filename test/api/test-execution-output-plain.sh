#!/bin/bash
# set -x
#test output from /api/execution/{id}/output

DIR=$(cd `dirname $0` && pwd)
source $DIR/include.sh

####
# Setup: create simple adhoc command execution to provide execution ID.
####


proj="test"
runurl="${APIURL}/project/${proj}/run/command"
params="exec=echo+%22%27testing+execution+%3Coutput%3E+api-plain+line+1%27%22+;sleep+2;echo+line+2;sleep+2;echo+line+3;sleep+2;echo+line+4+final"

expectfile=$DIR/expect-exec-output-plain.txt

cat > $expectfile <<END
testing execution <output> api-plain line 1
line 2
line 3
line 4 final
END

# get listing
docurl -X POST ${runurl}?${params} > $DIR/curl.out
if [ 0 != $? ] ; then
    errorMsg "ERROR: failed query request"
    exit 2
fi

#select id
execid=$(jq -r ".execution.id" < $DIR/curl.out)

if [ -z "$execid" ] ; then
    errorMsg "FAIL: expected execution id"
    exit 2
fi


####
# Test: receive output in plain text
####

# now submit req
runurl="${APIURL}/execution/${execid}/output.text"

echo "TEST: /api/execution/${execid}/output.text using lastmod ..."

outfile=$DIR/test-exec-output-plain.txt
doff=0
ddone="false"
dlast=0
dmax=20
dc=0
while [[ $ddone == "false" && $dc -lt $dmax ]]; do
    #statements
    params="offset=$doff&lastmod=$dlast"

    # get listing
    docurl -D $DIR/headers.out ${runurl}?${params} > $DIR/curl.out
    if [ 0 != $? ] ; then
        errorMsg "ERROR: failed query request"
        exit 2
    fi
    grep "HTTP/1.1 200" -q $DIR/headers.out
    if [ 0 != $? ] ; then
        errorMsg "ERROR: failed query request"
        exit 2
    fi

    ocount=$(wc -l $DIR/curl.out)

    #output text

    if [[ -r $DIR/curl.out ]]; then
        cat $DIR/curl.out >> $outfile
        cat $DIR/curl.out
        rm $DIR/curl.out
    fi

    unmod=$(grep 'X-Rundeck-ExecOutput-Unmodified:' $DIR/headers.out | cut -d' ' -f 2 | tr -d "\r\n")
    doff=$(grep 'X-Rundeck-ExecOutput-Offset:' $DIR/headers.out | cut -d' ' -f 2 | tr -d "\r\n")
    dlast=$(grep 'X-Rundeck-ExecOutput-LastModifed:' $DIR/headers.out | cut -d' ' -f 2 | tr -d "\r\n")
    ddone=$(grep 'X-Rundeck-Exec-Completed:' $DIR/headers.out | cut -d' ' -f 2 | tr -d "\r\n")
    #echo "unmod $unmod"
    #echo "doff $doff"
    #echo "dlast $dlast"
    #echo "ddone $ddone"
    if [[ $unmod == "true" ]]; then
        #echo "unmodifed, sleep 3..."
        sleep 2
    else
        #echo "$ocount lines, sleep 1"
        if [[ $ddone != "true" ]]; then
            sleep 1
        fi
    fi
    dc=$(( $dc + 1 ))

done

if [[ $ddone != "true" ]]; then
    errorMsg "ERROR: not all output was received in $dc requests"
    exit 2
fi

diff --ignore-all-space -q $expectfile $outfile
if [[  0 != $? ]] ; then
    errorMsg "ERROR: unexpected output"
    exit 2
fi

rm $expectfile $outfile

##wait for exec to finish...
api_waitfor_execution $execid || fail "Waiting for $execid to finish"
$SHELL $SRC_DIR/api-expect-exec-success.sh $execid || exit 2

echo "OK"
