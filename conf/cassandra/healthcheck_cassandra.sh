#!/bin/bash

if [ $(nodetool statusgossip) = "running" ]
then
	exit 0
fi
	exit 1
