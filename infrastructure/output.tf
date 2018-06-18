output "microserviceName" {
  value = "${var.product}-${var.component}"
}

output "vaultName" {
  value = "${module.key_vault.key_vault_name}"
}

output "vaultUri" {
  value = "${module.key_vault.key_vault_uri}"
}

output "idam_api_url" {
  value = "http://${var.idam_api_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"

}

output "s2s_url" {
  value = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
}
