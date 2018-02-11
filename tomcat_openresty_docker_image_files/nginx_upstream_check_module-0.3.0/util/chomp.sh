#!/bin/sh

for file in *
do
    if [ -d "$file" ]
    then
        continue
    fi

    ruby util/chomp.rb < $file > /tmp/tt
    mv /tmp/tt $file
done

rm -f /tmp/tt
