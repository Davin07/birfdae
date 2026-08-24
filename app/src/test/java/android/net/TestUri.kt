package android.net

/**
 * Concrete [Uri] subclass for JVM unit tests.
 *
 * Lives in the android.net package because [Uri]'s constructor is package-private. Instances are
 * allocated via Unsafe without invoking any constructor or static initializer (Uri's static init
 * throws RuntimeException("Stub!") in the mockable android.jar), so every method returns
 * null/default.
 */
open class TestUri : Uri() {
    override fun isHierarchical(): Boolean = false

    override fun isRelative(): Boolean = false

    override fun getScheme(): String? = "content"

    override fun getSchemeSpecificPart(): String? = null

    override fun getEncodedSchemeSpecificPart(): String? = null

    override fun getAuthority(): String? = null

    override fun getEncodedAuthority(): String? = null

    override fun getUserInfo(): String? = null

    override fun getEncodedUserInfo(): String? = null

    override fun getHost(): String? = null

    override fun getPort(): Int = -1

    override fun getPath(): String? = null

    override fun getEncodedPath(): String? = null

    override fun getQuery(): String? = null

    override fun getEncodedQuery(): String? = null

    override fun getFragment(): String? = null

    override fun getEncodedFragment(): String? = null

    @Suppress("USELESS_ELVIS")
    override fun getPathSegments(): List<String> = emptyList()

    override fun getLastPathSegment(): String? = null

    override fun buildUpon(): Builder = throw UnsupportedOperationException("TestUri cannot be built upon")

    override fun toString(): String = "content://test/backup"

    override fun describeContents(): Int = 0

    override fun writeToParcel(
        dest: android.os.Parcel,
        flags: Int,
    ) {
        throw UnsupportedOperationException("TestUri is not parcelable")
    }
}
