#!/usr/bin/env bash
#
#   Copyright 2019 yesday.github.io
#
#   Licensed under the Apache License, Version 2.0 (the "License");
#   you may not use this file except in compliance with the License.
#   You may obtain a copy of the License at
#
#       http://www.apache.org/licenses/LICENSE-2.0
#
#   Unless required by applicable law or agreed to in writing, software
#   distributed under the License is distributed on an "AS IS" BASIS,
#   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
#   See the License for the specific language governing permissions and
#   limitations under the License.
#

if [ -z "$YDSU_HOME" ]; then
  YDSU_HOME="$HOME/appdata/ydsu"
fi
INTERNAL_MODULES_REPOSITORY="${YDSU_HOME}/source/yesday/ydsu"
INTERNAL_MODULES_BASE_DIR="${INTERNAL_MODULES_REPOSITORY}/src/main/groovy/ydsu/module"
if [ -z "$YDSU_REPO" ]; then
  YDSU_REPO=https://github.com/yesday/ydsu.git
fi

echo ''
echo '              _'
echo '             | |'
echo '  _   _    __| |  ___   _   _'
echo ' | | | |  / _` | / __| | | | |'
echo ' | |_| | | (_| | \__ \ | |_| |'
echo '  \__, |  \__,_| |___/  \__,_|'
echo '   __/ |'
echo '  |___/'
echo ''
echo ''
echo '                                                                 Now attempting installation...'
echo ''
echo ''

echo "Looking for a previous installation of ydsu..."
if [ -d "$INTERNAL_MODULES_REPOSITORY" ]; then
  echo "ydsu found."
  echo ""
  echo "======================================================================================================"
  echo " You already have ydsu installed."
  echo " ydsu was found at:"
  echo ""
  echo "    ${INTERNAL_MODULES_REPOSITORY}"
  echo ""
  echo " Please consider running the following if you need to upgrade."
  echo ""
  echo "    $ ydsuPull"
  echo ""
  echo " Alternatively, to reset the existing installation perform the following steps."
  echo ""
  echo "    $ ydsuUninstall"
  echo "    Run this script again"
  echo ""
  echo "======================================================================================================"
  echo ""
  exit 0
fi

echo "Looking for git..."
if [ -z $(which git) ]; then
  echo "Not found."
  echo "======================================================================================================"
  echo " Please install git on your system using your favourite package manager."
  echo ""
  echo " Restart after installing git."
  echo "======================================================================================================"
  echo ""
  exit 1
fi

echo "Looking for groovy..."
if [ -z $(which groovy) ]; then
  echo "Not found."
  echo "======================================================================================================"
  echo " Please install groovy on your system using your favourite package manager."
  echo ""
  echo " Restart after installing groovy."
  echo "======================================================================================================"
  echo ""
  exit 1
fi

echo "Looking for ansible..."
if [ -z $(which ansible) ]; then
  echo "Not found."
  echo "======================================================================================================"
  echo " Please install ansible on your system using your favourite package manager."
  echo ""
  echo " Restart after installing ansible."
  echo "======================================================================================================"
  echo ""
  exit 1
fi

echo "Installing ydsu..."

# Create directory structure

echo "Create distribution directories..."
# The following `cd; cd -` prevents the error caused by (optionally) re-setting the ydsu directory:
# shell-init: error retrieving current directory: getcwd: cannot access parent directories: No such file or directory
cd
cd -
mkdir -p "${YDSU_HOME}/source/yesday/"
if [[ $YDSU_REPO == /* ]]; then
  # if it's a local directory then just copy it's contents
  cp -R ${YDSU_REPO} "${INTERNAL_MODULES_REPOSITORY}"
else
  git clone ${YDSU_REPO} "${INTERNAL_MODULES_REPOSITORY}"
fi

PATH="$PATH:\
${INTERNAL_MODULES_BASE_DIR}/manage:\
${INTERNAL_MODULES_BASE_DIR}/util"

"${INTERNAL_MODULES_BASE_DIR}/manage/ydsuLoadModules"
