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

output "spring_datasource_url" {
  value = "jdbc:postgresql://${module.db.host_name}:${module.db.postgresql_listen_port}/${module.db.postgresql_database}?ssl=true"
}

output "spring_datasource_username" {
  value = "${module.db.user_name}"
}

output "spring_datasource_password" {
  value = "${module.db.postgresql_password}"
}
