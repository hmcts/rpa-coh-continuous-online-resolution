output "idam_api_url" {
  value = "${var.idam_api_url}"
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

output "microservicekey-coh-cor" {
  value        = "${data.azurerm_key_vault_secret.s2s_key.value}"
}