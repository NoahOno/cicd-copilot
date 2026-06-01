#!/bin/bash
set -euo pipefail
TEMPLATES_DIR="${1:-$(dirname "$0")/../templates/k8s}"
OUTPUT_DIR="${2:-./rendered}"
export REGISTRY="${REGISTRY:-k3d-cicd-registry:5000}"
export IMAGE_TAG="${IMAGE_TAG:-latest}"
echo "Rendering K8s templates..."
echo "  REGISTRY = ${REGISTRY}"
echo "  IMAGE_TAG = ${IMAGE_TAG}"
mkdir -p "${OUTPUT_DIR}"
find "${TEMPLATES_DIR}" -name '*.tmpl' | while read -r tmpl; do
    rel="${tmpl#${TEMPLATES_DIR}/}"
    rel="${rel%.tmpl}"
    out="${OUTPUT_DIR}/${rel}"
    mkdir -p "$(dirname "${out}")"
    envsubst < "${tmpl}" > "${out}"
    echo "  + ${rel}"
done
find "${TEMPLATES_DIR}" -name '*.yaml' | while read -r yaml; do
    rel="${yaml#${TEMPLATES_DIR}/}"
    out="${OUTPUT_DIR}/${rel}"
    mkdir -p "$(dirname "${out}")"
    cp "${yaml}" "${out}"
    echo "  + ${rel} (copied)"
done
echo "Done! Files in ${OUTPUT_DIR}:"
find "${OUTPUT_DIR}" -type f | sort
