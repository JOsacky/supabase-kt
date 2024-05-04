package io.github.jan.supabase.functions

/**
 * The region where the function is invoked.
 * @param value The value of the region
 */
enum class FunctionRegion(val value: String) {
    Any("any"),
    ApNortheast1("ap-northeast-1"),
    ApNortheast2("ap-northeast-2"),
    ApSouth1("ap-south-1"),
    ApSoutheast1("ap-southeast-1"),
    ApSoutheast2("ap-southeast-2"),
    CaCentral1("ca-central-1"),
    EuCentral1("eu-central-1"),
    EuWest1("eu-west-1"),
    EuWest2("eu-west-2"),
    EuWest3("eu-west-3"),
    SaEast1("sa-east-1"),
    UsEast1("us-east-1"),
    UsWest1("us-west-1"),
    UsWest2("us-west-2"),
}