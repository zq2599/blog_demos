#!/bin/bash

echo "Starting jupyter"
/opt/conda/bin/jupyter notebook --notebook-dir=/opt/notebooks --ip='0.0.0.0' --port=8888 --allow-root --no-browser
