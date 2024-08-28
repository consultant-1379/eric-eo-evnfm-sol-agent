{{/* vim: set filetype=mustache: */}}
{{/*
Expand the name of the chart.
*/}}
{{- define "eric-eo-evnfm-nbi.name" -}}
{{- default .Chart.Name .Values.nameOverride | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{/*
Create a default fully qualified app name.
We truncate at 63 chars because some Kubernetes name fields are limited to this (by the DNS naming spec).
*/}}
{{- define "eric-eo-evnfm-nbi.fullname" -}}
{{- if .Values.fullnameOverride -}}
{{- .Values.fullnameOverride | trunc 63 | trimSuffix "-" -}}
{{- else -}}
{{- template "eric-eo-evnfm-nbi.name" . -}}
{{- end -}}
{{- end -}}

{{/*
Create chart name and version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm-nbi.chart" -}}
{{- printf "%s-%s" .Chart.Name .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create hazelcast cluster name.
*/}}
{{- define "eric-eo-evnfm-nbi.hazelcast.cluster" -}}
{{- printf "hazelcast_cluster-%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Create main image registry url
*/}}
{{- define "eric-eo-evnfm-nbi.mainImagePath" -}}
  {{- include "eric-eo-evnfm-library-chart.mainImagePath" (dict "ctx" . "svcRegistryName" "evnfmNBI") -}}
{{- end -}}

{{/*
Create Ericsson Product Info
*/}}
{{- define "eric-eo-evnfm-nbi.helm-annotations" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations" . -}}
{{- end -}}

{{/*
Create prometheus info
*/}}
{{- define "eric-eo-evnfm-nbi.prometheus" -}}
  {{- include "eric-eo-evnfm-library-chart.prometheus" . -}}
{{- end -}}

{{/*
Create Ericsson product app.kubernetes.io info
*/}}
{{- define "eric-eo-evnfm-nbi.kubernetes-io-info" -}}
  {{- include "eric-eo-evnfm-library-chart.kubernetes-io-info" . -}}
{{- end -}}

{{/*
Create image pull secrets
*/}}
{{- define "eric-eo-evnfm-nbi.pullSecrets" -}}
  {{- include "eric-eo-evnfm-library-chart.pullSecrets" . -}}
{{- end -}}

{{/*
Create pullPolicy for eric-eo-evnfm-nbi container
*/}}
{{- define "eric-eo-evnfm-nbi.imagePullPolicy" -}}
    {{- if .Values.imageCredentials.registry -}}
        {{- if .Values.imageCredentials.registry.imagePullPolicy -}}
            {{- print .Values.imageCredentials.registry.imagePullPolicy -}}
        {{- end -}}
    {{- else if .Values.imageCredentials.pullPolicy -}}
        {{- print .Values.imageCredentials.pullPolicy -}}
    {{- else if .Values.global.registry.imagePullPolicy -}}
        {{- print .Values.global.registry.imagePullPolicy -}}
    {{- end -}}
{{- end -}}

{{/*
Create chart version as used by the chart label.
*/}}
{{- define "eric-eo-evnfm-nbi.version" -}}
{{- printf "%s" .Chart.Version | replace "+" "_" | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
Kubernetes labels
*/}}
{{- define "eric-eo-evnfm-nbi.kubernetes-labels" -}}
app.kubernetes.io/name: {{ include "eric-eo-evnfm-nbi.name" . }}
app.kubernetes.io/instance: {{ .Release.Name | quote }}
app.kubernetes.io/version: {{ include "eric-eo-evnfm-nbi.version" . }}
{{- end -}}

{{/*
Common labels
*/}}
{{- define "eric-eo-evnfm-nbi.labels" -}}
  {{- $kubernetesLabels := include "eric-eo-evnfm-nbi.kubernetes-labels" . | fromYaml -}}
  {{- $globalLabels := (.Values.global).labels -}}
  {{- $serviceLabels := .Values.labels -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $kubernetesLabels $globalLabels $serviceLabels)) }}
{{- end -}}

{{/*
Merged labels for extended defaults
*/}}
{{- define "eric-eo-evnfm-nbi.labels.extended-defaults" -}}
  {{- $extendedLabels := dict -}}
  {{- $_ := set $extendedLabels "app" (include "eric-eo-evnfm-nbi.name" .) -}}
  {{- $_ := set $extendedLabels "chart" (include "eric-eo-evnfm-nbi.chart" .) -}}
  {{- $_ := set $extendedLabels "release" (.Release.Name) -}}
  {{- $_ := set $extendedLabels "heritage" (.Release.Service) -}}
  {{- $_ := set $extendedLabels "logger-communication-type" "direct" -}}
  {{- $commonLabels := include "eric-eo-evnfm-nbi.labels" . | fromYaml -}}
  {{- $serviceMesh := include "eric-eo-evnfm-nbi.service-mesh-inject" . | fromYaml -}}
  {{- include "eric-eo-evnfm-library-chart.mergeLabels" (dict "location" .Template.Name "sources" (list $commonLabels $extendedLabels $serviceMesh)) | trim }}
{{- end -}}

{{/*
Check global.security.tls.enabled
*/}}
{{- define "eric-eo-evnfm-nbi.global-security-tls-enabled" -}}
  {{- include "eric-eo-evnfm-library-chart.global-security-tls-enabled" . -}}
{{- end -}}

{{/*
DR-D470217-007-AD This helper defines whether this service enter the Service Mesh or not.
*/}}
{{- define "eric-eo-evnfm-nbi.service-mesh-enabled" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-enabled" . -}}
{{- end -}}

{{/*
DR-D470217-011 This helper defines the annotation which bring the service into the mesh.
*/}}
{{- define "eric-eo-evnfm-nbi.service-mesh-inject" -}}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-inject" . -}}
{{- end -}}

{{/*
GL-D470217-080-AD
This helper captures the service mesh version from the integration chart to
annotate the workloads so they are redeployed in case of service mesh upgrade.
*/}}
{{- define "eric-eo-evnfm-nbi.service-mesh-version" }}
  {{- include "eric-eo-evnfm-library-chart.service-mesh-version" . -}}
{{- end -}}

{{/*
This helper defines log level for Service Mesh.
*/}}
{{- define "eric-eo-evnfm-nbi.service-mesh-logs" }}
{{- if .Values.highAvailability.debug }}
sidecar.istio.io/logLevel: debug
{{- else -}}
sidecar.istio.io/logLevel: info
{{- end -}}
{{- end -}}

{{/*
DR-D1123-124
Evaluating the Security Policy Cluster Role Name
*/}}
{{- define "eric-eo-evnfm-nbi.securityPolicy.reference" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.reference" . -}}
{{- end -}}

{{/*
Define probes property
*/}}
{{- define "eric-eo-evnfm-nbi.probes" -}}
{{- $default := .Values.probes -}}
{{- if .Values.probing }}
  {{- if .Values.probing.liveness }}
    {{- if .Values.probing.liveness.nbi }}
      {{- $default := mergeOverwrite $default.nbi.livenessProbe .Values.probing.liveness.nbi  -}}
    {{- end }}
  {{- end }}
  {{- if .Values.probing.readiness }}
    {{- if .Values.probing.readiness.nbi }}
      {{- $default := mergeOverwrite $default.nbi.readinessProbe .Values.probing.readiness.nbi  -}}
    {{- end }}
  {{- end }}
{{- end }}
{{- $default | toJson -}}
{{- end -}}

{{/*
To support Dual stack.
*/}}
{{- define "eric-eo-evnfm-nbi.internalIPFamily" -}}
  {{- include "eric-eo-evnfm-library-chart.internalIPFamily" . -}}
{{- end -}}

{{/*
Define podPriority property
*/}}
{{- define "eric-eo-evnfm-nbi.podPriority" -}}
  {{- include "eric-eo-evnfm-library-chart.podPriority" ( dict "ctx" . "svcName" "nbi" ) -}}
{{- end -}}

{{/*
Define tolerations property
*/}}
{{- define "eric-eo-evnfm-nbi.tolerations.nbi" -}}
  {{- include "eric-eo-evnfm-library-chart.merge-tolerations" (dict "root" . "podbasename" "nbi" ) -}}
{{- end -}}

{{/*
Create Ericsson product specific annotations
*/}}
{{- define "eric-eo-evnfm-nbi.helm-annotations_product_name" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_name" . -}}
{{- end -}}

{{- define "eric-eo-evnfm-nbi.helm-annotations_product_number" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_number" . -}}
{{- end -}}

{{- define "eric-eo-evnfm-nbi.helm-annotations_product_revision" -}}
  {{- include "eric-eo-evnfm-library-chart.helm-annotations_product_revision" . -}}
{{- end -}}

{{/*
Create a dict of annotations for the product information (DR-D1121-064, DR-D1121-067).
*/}}
{{- define "eric-eo-evnfm-nbi.product-info" }}
ericsson.com/product-name: {{ template "eric-eo-evnfm-nbi.helm-annotations_product_name" . }}
ericsson.com/product-number: {{ template "eric-eo-evnfm-nbi.helm-annotations_product_number" . }}
ericsson.com/product-revision: {{ template "eric-eo-evnfm-nbi.helm-annotations_product_revision" . }}
{{- end }}

{{/*
Common annotations
*/}}
{{- define "eric-eo-evnfm-nbi.annotations" -}}
  {{- $productInfo := include "eric-eo-evnfm-nbi.helm-annotations" . | fromYaml -}}
  {{- $globalAnn := (.Values.global).annotations -}}
  {{- $serviceAnn := .Values.annotations -}}
  {{- include "eric-eo-evnfm-library-chart.mergeAnnotations" (dict "location" .Template.Name "sources" (list $productInfo $globalAnn $serviceAnn)) | trim }}
{{- end -}}

{{/*
Create Service Mesh Ingress enabling option
*/}}
{{- define "eric-eo-evnfm-nbi.service-mesh-ingress-enabled" -}}
  {{ if .Values.global.serviceMesh }}
    {{ if .Values.global.serviceMesh.ingress }}
      {{ if .Values.global.serviceMesh.ingress.enabled }}
        {{- print "true" -}}
      {{ else }}
        {{- print "false" -}}
      {{- end -}}
    {{ else }}
      {{- print "false" -}}
    {{- end -}}
  {{ else }}
    {{- print "false" -}}
  {{ end }}
{{- end}}

{{/*
Create fsGroup Values DR-1123-136
*/}}
{{- define "eric-eo-evnfm-nbi.fsGroup" -}}
{{- include "eric-eo-evnfm-library-chart.fsGroup" . -}}
{{- end -}}

{{/*
DR-D470222-010
Configuration of Log Collection Streaming Method
*/}}
{{- define "eric-eo-evnfm-nbi.log.streamingMethod" -}}
{{- $defaultMethod := "dual" }}
{{- $streamingMethod := (.Values.log).streamingMethod }}
    {{- if not $streamingMethod }}
        {{- if (.Values.global.log).streamingMethod -}}
            {{- $streamingMethod = (.Values.global.log).streamingMethod }}
        {{- else -}}
            {{- $streamingMethod = $defaultMethod -}}
         {{- end }}
    {{- end }}

    {{- if or (eq $streamingMethod "direct") (eq $streamingMethod "indirect") }}
        {{- $streamingMethod -}}
    {{- else }}
        {{- $defaultMethod -}}
    {{- end }}
{{- end }}

{{/*
Define ServiceAccount template
*/}}
{{- define "eric-eo-evnfm-nbi.serviceAccount.name" -}}
  {{- printf "%s-sa" (include "eric-eo-evnfm-nbi.name" .) | trunc 63 | trimSuffix "-" -}}
{{- end -}}

{{/*
DR-D1123-134
Rolekind parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-nbi.securityPolicy.rolekind" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolekind" . -}}
{{- end -}}

{{/*
DR-D1123-134
Rolename parameter for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-nbi.securityPolicy.rolename" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolename" . -}}
{{- end -}}

{{/*
DR-D1123-134
RoleBinding name for generation of role bindings for admission control in OpenShift environment
*/}}
{{- define "eric-eo-evnfm-nbi.securityPolicy.rolebinding.name" -}}
  {{- include "eric-eo-evnfm-library-chart.securityPolicy.rolebinding.name" . -}}
{{- end -}}

{{/*
Define nodeSelector property
*/}}
{{- define "eric-eo-evnfm-nbi.nodeSelector" -}}
  {{- include "eric-eo-evnfm-library-chart.nodeSelector" . -}}
{{- end -}}

{{/*
Istio excludeInboundPorts. Inbound ports to be excluded from redirection to Envoy.
*/}}
{{- define "eric-eo-evnfm-nbi.excludeInboundPorts" -}}
  {{- include "eric-eo-evnfm-library-chart.excludeInboundPorts" . -}}
{{- end -}}

{{/*
Create ipV6 support for eric-eo-evnfm-nbi hazelcast
*/}}
{{- define "eric-eo-evnfm-nbi.hazelcast-ipv6-enabled" -}}
  {{ if .Values.global.support }}
    {{ if .Values.global.support.ipv6 }}
      {{ if eq .Values.global.support.ipv6.enabled true}}
        {{- print "true" -}}
      {{ else }}
        {{- print "false" -}}
      {{- end -}}
    {{ else }}
      {{- print "false" -}}
    {{- end -}}
  {{ else }}
    {{- print "false" -}}
  {{ end }}
{{- end}}

{{/*
Define vhost for envoy filter
*/}}
{{- define "eric-eo-evnfm-nbi.envoy.retryAfter.vhost" }}
   {{- $serviceName := .Values.highAvailability.serviceMesh.envoyFilter.orchestrator.serviceName  -}}
   {{- $namespace := .Release.Namespace -}}
   {{- $port := .Values.highAvailability.serviceMesh.envoyFilter.orchestrator.port -}}
   {{- printf "%s.%s.svc.cluster.local:%v" $serviceName $namespace $port -}}
{{- end }}