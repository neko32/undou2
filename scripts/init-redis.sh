#!/bin/bash

function find_item() {
    id=$1
    redis-cli hgetall "${id}"
}

function add_item() {
    id=$1
    shift
    mediaId=$1
    shift
    name=$1
    shift
    length=$1
    shift
    url=$1
    shift
    count=$1
    shift
    alt=$1
    if [ "${alt}" != "NA" ];
    then
        redis-cli hmset "${id}" mediaId ${mediaId} name "${name}" length "${length}" url ${url} countPlayed ${count} altMediaId "${alt}"
    else
        redis-cli hmset "${id}" mediaId ${mediaId} name "${name}" length "${length}" url ${url} countPlayed ${count}
    fi

    echo "Item ${id} has been added"
    find_item "${id}"
}


add_item "undou2:media:1" 1 "radio_taiso" 380 "https://www.youtube.com/embed/cZWzB9vcO6w?rel=0&autoplay=1" 0 3
add_item "undou2:media:2" 2 "fitnesse_kizuna" 602 "https://youtu.be/qOiDlprXF2w" 0 "NA"
add_item "undou2:media:3" 3 "radio_taiso_alt_local" 380 "file://radio_taiso.mp4" 0 "NA"

echo "done."
