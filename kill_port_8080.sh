#!/bin/bash

lsof -i tcp:8080 | awk 'NR!=1 {print $2}' | xargs -r kill 2>/dev/null

lsof -i tcp:5005 | awk 'NR!=1 {print $2}' | xargs -r kill 2>/dev/null