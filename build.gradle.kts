plugins { alias(libs.plugins.gitVersioning) }

apply { plugin("me.qoomon.git-versioning") }

gitVersioning.apply {
  refs {
    considerTagsOnBranches = System.getenv()["CI"]?.toBoolean() ?: true
    tag("v(?<version>.+)") { version = "\${ref.version}" }
    branch(".+") { version = "\${ref}" }
  }
}
