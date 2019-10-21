provider "azurerm" {
  version = "1.22.1"
}

locals {
  app_full_name = "${var.product}-${var.component}"
  ase_name = "${data.terraform_remote_state.core_apps_compute.ase_name[0]}"
  local_env = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "aat" : "saat" : var.env}"
  shared_vault_name = "${var.shared_product_name}-${local.local_env}"
  s2s_vault_url = "https://s2s-${local.local_env}.vault.azure.net/"
  local_ase              = "${(var.env == "preview" || var.env == "spreview") ? (var.env == "preview" ) ? "core-compute-aat" : "core-compute-saat" : local.ase_name}"
  s2s_url                = "http://rpe-service-auth-provider-${local.local_env}.service.${local.local_ase}.internal"

  previewVaultName = "${local.app_full_name}-aat"
  nonPreviewVaultName = "${local.app_full_name}-${var.env}"
  vaultName = "${(var.env == "preview" || var.env == "spreview") ? local.previewVaultName : local.nonPreviewVaultName}"
}

module "app" {
  source = "git@github.com:hmcts/cnp-module-webapp?ref=master"
  product = "${local.app_full_name}"
  location = "${var.location}"
  env = "${var.env}"
  ilbIp = "${var.ilbIp}"
  subscription = "${var.subscription}"
  capacity     = "${var.capacity}"
  is_frontend = false
  additional_host_name = "${local.app_full_name}-${var.env}.service.${var.env}.platform.hmcts.net"
  https_only="false"
  common_tags  = "${var.common_tags}"
  asp_rg = "${var.shared_product_name}-${var.env}"
  asp_name = "${var.shared_product_name}-${var.env}"

  app_settings = {
    POSTGRES_HOST = "${module.db.host_name}"
    POSTGRES_PORT = "${module.db.postgresql_listen_port}"
    POSTGRES_DATABASE = "${module.db.postgresql_database}"
    POSTGRES_USER = "${module.db.user_name}"
    POSTGRES_PASSWORD = "${module.db.postgresql_password}"
    MAX_ACTIVE_DB_CONNECTIONS = 70

    # db
    SPRING_DATASOURCE_URL = "jdbc:postgresql://${module.db.host_name}:${module.db.postgresql_listen_port}/${module.db.postgresql_database}?ssl=true"
    SPRING_DATASOURCE_USERNAME = "${module.db.user_name}"
    SPRING_DATASOURCE_PASSWORD = "${module.db.postgresql_password}"

    ENABLE_DB_MIGRATE="false"

    # idam
    IDAM_API_URL = "${var.idam_api_url}"
    S2S_URL = "http://${var.s2s_url}-${local.local_env}.service.core-compute-${local.local_env}.internal"
    S2S_TOKEN = "${data.azurerm_key_vault_secret.s2s_secret.value}"
    S2S_NAMES_WHITELIST = "${var.s2s_names_whitelist}"

    # logging vars & healthcheck
    REFORM_SERVICE_NAME = "${local.app_full_name}"
    REFORM_TEAM = "${var.team_name}"
    REFORM_SERVICE_TYPE = "${var.app_language}"
    REFORM_ENVIRONMENT = "${var.env}"

    PACKAGES_NAME = "${local.app_full_name}"
    PACKAGES_PROJECT = "${var.team_name}"
    PACKAGES_ENVIRONMENT = "${var.env}"

    SCHEDULING_NOTIFIER_LOCK = "event-notifier-${var.scheduling_lock_env}"
    SCHEDULING_TRIGGER_LOCK = "event-trigger-${var.scheduling_lock_env}"

    LOG_OUTPUT = "${var.log_output}"
  }
}

module "db" {
  source = "git@github.com:hmcts/cnp-module-postgres?ref=master"
  product = "${local.app_full_name}-postgres-db"
  location = "${var.location}"
  env = "${var.env}"
  subscription = "${var.subscription}"
  postgresql_user = "${var.postgresql_user}"
  database_name = "${var.database_name}"
  sku_name = "GP_Gen5_2"
  sku_tier = "GeneralPurpose"
  storage_mb = "51200"
  common_tags  = "${var.common_tags}"
}

module "local_key_vault" {
  source = "git@github.com:hmcts/cnp-module-key-vault?ref=master"
  product = "${local.app_full_name}"
  env = "${var.env}"
  tenant_id = "${var.tenant_id}"
  object_id = "${var.jenkins_AAD_objectId}"
  resource_group_name = "${module.app.resource_group_name}"
  product_group_object_id = "5d9cd025-a293-4b97-a0e5-6f43efce02c0"
  common_tags  = "${var.common_tags}"
}

data "azurerm_key_vault" "shared_key_vault" {
  name = "${local.shared_vault_name}"
  resource_group_name = "${local.shared_vault_name}"
}

data "azurerm_key_vault" "s2s_vault" {
  name = "s2s-${local.local_env}"
  resource_group_name = "rpe-service-auth-provider-${local.local_env}"
}

data "azurerm_key_vault_secret" "s2s_secret" {
  name = "microservicekey-coh-cor"
  key_vault_id = "${data.azurerm_key_vault.s2s_vault.id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-USER" {
  name = "${var.component}-POSTGRES-USER"
  value = "${module.db.user_name}"
  key_vault_id = "${module.local_key_vault.key_vault_id}"
}

resource "azurerm_key_vault_secret" "POSTGRES-PASS" {
  name = "${var.component}-POSTGRES-PASS"
  value = "${module.db.postgresql_password}"
  key_vault_id = "${module.local_key_vault.key_vault_id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_HOST" {
  name = "${var.component}-POSTGRES-HOST"
  value = "${module.db.host_name}"
  key_vault_id = "${module.local_key_vault.key_vault_id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_PORT" {
  name = "${var.component}-POSTGRES-PORT"
  value = "${module.db.postgresql_listen_port}"
  key_vault_id = "${module.local_key_vault.key_vault_id}"
}

resource "azurerm_key_vault_secret" "POSTGRES_DATABASE" {
  name = "${var.component}-POSTGRES-DATABASE"
  value = "${module.db.postgresql_database}"
  key_vault_id = "${module.local_key_vault.key_vault_id}"
}
