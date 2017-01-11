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
        "kontaktregister-statistikk")
            image="difi/kontaktregister-statistikk-klient:${version}"
            ;;
        *)
            fail "Unknown service ${service}"
    esac
    echo ${image}
}

createService() {
    service=$1
    kontaktregisterurl=${2}
    statistikkingesturl=${3}
    version=${4-'latest'}
    requireArgument 'service'
    requireArgument 'kontaktregisterurl'
    requireArgument 'statistikkingesturl'
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
            --secret krr-stat-pumba \
            -p 8084:8080 \
            ${image} \
            --url.base.kontaktregister=${kontaktregisterurl} \
            --url.base.ingest.statistikk=${statistikkingesturl} \
            ) || fail "Failed to create service ${service}"
        ;;
    esac
}

updateService() {
    service=$1
    kontaktregisterurl=${2}
    statistikkingesturl=${3}
    version=${4-'latest'}
    requireArgument 'service'
    requireArgument 'kontaktregisterurl'
    requireArgument 'statistikkingesturl'
    echo -n "Updating service ${service} to version ${version}... "
    image=$(image ${service} ${version})
    output=$(sudo docker service inspect ${service}) || { echo "Service needs to be created"; createService ${service} ${kontaktregisterurl} ${statistikkingesturl} ${version} ; return; }
    output=$(sudo docker service update ${service} \
            --secret-add krr-stat-pumba \
            --args "--url.base.kontaktregister=${kontaktregisterurl} --url.base.statistikk=${statistikkurl}" ) \
            --args "--url.base.kontaktregister=${kontaktregisterurl} --url.base.ingest.statistikk=${statistikkingesturl}" ) \
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
        'kontaktregister-statistikk')
            url="http://${host}:8084"
            ;;
        *)
            echo -n "Unknown service \"${service}\""
            return 1
    esac
    curl -s ${url} --connect-timeout 3 --max-time 10 > /dev/null
}

update() {
    kontaktregisterurl=${1}
    statistikkingesturl=${2}
    version=${3-'latest'}
    requireArgument 'kontaktregisterurl'
    requireArgument 'statistikkingesturl'
    echo "Updating application to version ${version}..."
    updateService 'kontaktregister-statistikk' ${kontaktregisterurl} ${statistikkingesturl} ${version} || return $?
    echo "Application updated"
}

delete() {
    echo "Deleting application..."
    deleteService "kontaktregister-statistikk"
    echo "Application deleted"
}

createAndVerify() {
    version=${1-'latest'}
    create latest || return $?
    verify ${version} || return $?
}

verify() {
    version=${1-'latest'}
    waitForServiceToBeAvailable 'kontaktregister-statistikk' || return $?
}

case "${1}" in *)
        function="${1}"
        shift
        ${function} "${@}"
        ;;
esac