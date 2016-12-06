#!/usr/bin/env bash

fail() {
    ret=$?
    message=${1-"[Failed (${ret})]"}
    echo ${message}
    return ${ret}
}

warn() {
    message=$1
    echo ${message}
}

ok() {
    echo "[OK]"
}

dotSleep() {
    length=${1-1}
    echo -n "."
    sleep ${length};
}

requireArgument() {
    test -z ${!1} && fail "Missing argument '${1}'"
}

image() {
    service=$1
    version=${2-'latest'}
    requireArgument 'service'
    case "${service}" in
        "kontaktregister_statistikk")
            image="difi/kontaktregister-statistikk-klient:${version}"
            ;;
        *)
            fail "Unknown service ${service}"
    esac
    echo ${image}
}

createService() {
    service=$1
    version=${2-'latest'}
    requireArgument 'service'
    network='statistics'
    echo -n "Creating service ${service} of version ${version}... "
    image=$(image ${service} ${version})
    case ${service} in
     kontaktregister_statistikk)
        output=$(sudo docker service create \
            --network ${network} \
            --mode replicated \
            --replicas 1 \
            --name ${service} \
            -p 8084:8080 \
            ${image}) \
            || fail "Failed to create service ${service}"
        ;;
    esac

    if [ $? -eq 0 ]
    then
      fail "Failed creating service ${version}"
    else
      ok
    fi
}

updateService() {
    service=$1
    version=${2-'latest'}
    requireArgument 'service'
    echo -n "Updating service ${service} to version ${version}... "
    image=$(image ${service} ${version})
    output=$(sudo docker service inspect ${service}) || { echo "Service needs to be created"; createService ${service} ${version}; return; }
    output=$(sudo docker service update --image ${image} ${service}) \
        && ok || fail
    verify ${version} || return $?
}

isServiceUpdateCompleted() {
    service="${1}"
    requireArgument 'service'
    [ "$(sudo docker service inspect ${service} -f '{{.UpdateStatus.State}}')" == "completed" ]
}

deleteService() {
    service=$1
    requireArgument 'service'
    echo -n "Deleting service ${service}... "
    output=$(sudo docker service rm ${service}) \
        && ok || fail
}

waitForServiceToBeAvailable() {
    service=$1
    requireArgument 'service'
    host=${2-'localhost'}
    echo -n "Waiting for service \"${service}\" to be available: "
    status=false
    for i in $(seq 1 200); do
        isServiceAvailable ${service} ${host}
        ret=$?
        [ ${ret} -eq 7 -o ${ret} -eq 27 ] && dotSleep; # Connect failure or request timeout
        [ ${ret} -eq 0 ] && { status=true; break; }
        [ ${ret} -eq 1 ] && break # Unknown service
    done
    ${status} && ok || fail
}

isServiceAvailable() {
    service=$1
    requireArgument 'service'
    host=${2-'localhost'}
    case "${service}" in
        'kontaktregister_statistikk')
            url="http://${host}:8084"
            ;;
        *)
            echo -n "Unknown service \"${service}\""
            return 1
    esac
    curl -s ${url} --connect-timeout 3 --max-time 10 > /dev/null
}

update() {
    version=${1-'latest'}
    echo "Updating application to version ${version}..."
    updateService 'kontaktregister_statistikk' ${version} || return $?
    echo "Application updated"
}

delete() {
    echo "Deleting application..."
    deleteService "kontaktregister_statistikk"
    echo "Application deleted"
}

createAndVerify() {
    version=${1-'latest'}
    create latest || return $?
    verify ${version} || return $?
}

verify() {
    version=${1-'latest'}
    waitForServiceToBeAvailable 'kontaktregister_statistikk' || return $?
}

case "${1}" in *)
        function="${1}"
        shift
        ${function} "${@}"
        ;;
esac