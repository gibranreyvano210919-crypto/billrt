# Update API Port for Catatan Tagihan

The user wants to change the port number from `8890` to `8885` specifically for the `catatan_tagihan` API.

## Proposed Changes

### [Component Name] :app

#### [MODIFY] [ApiService.kt](file:///D:/AndroidProjects/billrt_lite/app/src/main/java/com/linkbit/billrt/ApiService.kt)

- Update the `@GET` annotation for `getCatatanTagihan` to use the absolute URL with port `8885`.

```kotlin
    @GET("http://112.78.170.196:8885/billrt/api/index.php?tabel=catatan_tagihan")
    fun getCatatanTagihan(@Query("search") search: String, @Query("bulan") bulan: Int, @Query("tahun") tahun: Int): Call<GroupedKasResponse>
```

## Verification Plan

### Manual Verification
- Verify that `RiwayatKasFragment` now calls the API on port `8885` for the "Riwayat Kas" list.
