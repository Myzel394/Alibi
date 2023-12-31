package app.myzel394.alibi.ui

import android.os.Build
import androidx.compose.ui.unit.dp

val BIG_PRIMARY_BUTTON_SIZE = 64.dp
val MAX_AMPLITUDE = 20000
val SUPPORTS_DARK_MODE_NATIVELY = Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q
val RECORDER_SUBFOLDER_NAME = ".recordings"

// TODO: Fix!
val SUPPORTS_SCOPED_STORAGE = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
val MEDIA_RECORDINGS_PREFIX = "alibi-recording-"
val RECORDER_MEDIA_SELECTED_VALUE = "_'media"
val RECORDER_INTERNAL_SELECTED_VALUE = "_'internal"

// You are not allowed to change the constants below.
// If you do so, you will be blocked on GitHub.
const val REPO_URL = "https://github.com/Myzel394/Alibi"
const val TRANSLATION_HELP_URL = "https://crowdin.com/project/alibi"
const val GITHUB_SPONSORS_URL = "https://github.com/sponsors/Myzel394"
const val PUBLIC_KEY = """-----BEGIN PGP PUBLIC KEY BLOCK-----

mDMEZTfvnhYJKwYBBAHaRw8BAQdAi2AiLsTaBoLhnQtY5vi3xBU/H428wbNfBSe+
2dhz3r60Jk15emVsMzk0IDxnaXRodWIuN2Eyb3BAc2ltcGxlbG9naW4uY28+iJkE
ExYKAEEWIQR9BS8nNHwqrNgV0B3NE0dCwel5WQUCZTfvngIbAwUJEswDAAULCQgH
AgIiAgYVCgkICwIEFgIDAQIeBwIXgAAKCRDNE0dCwel5WcS8AQCf9g6eEaut1suW
l6jCLIg3b1nWLckmLJaonM6PruUtigEAmVnFOxMpOZEIcILT8CD2Riy+IVN9gTNH
qOHnaFsu8AK4OARlN++eEgorBgEEAZdVAQUBAQdAe4ffDtRundKH9kam746i2TBu
P9sfb3QVi5QqfK+bek8DAQgHiH4EGBYKACYWIQR9BS8nNHwqrNgV0B3NE0dCwel5
WQUCZTfvngIbDAUJEswDAAAKCRDNE0dCwel5WWwSAQDj4ZAl6bSqwbcptEMYQaPM
MMhMafm446MjkhQioeXw+wEAzA8mS6RBx7IZvu1dirmFHXOEYJclwjyQhNs4uEjq
/Ak=
=ICHe
-----END PGP PUBLIC KEY BLOCK-----"""
const val PUBLIC_KEY_FINGERPRINT = "7D05 2F27 347C 2AAC D815  D01D CD13 4742 C1E9 7959"
val CRYPTO_DONATIONS = mapOf(
    "Bitcoin" to "bc1qw054829yj8e2u8glxnfcg3w22dkek577mjt5x6",
    "Bitcoin Cash" to "qr9s64vfqedvurfef9ykf7szchmt0xyvnga452fc8l",
    "Ethereum" to "0xbb5E631c03C65334d1d9EfBCD926DC1265CC20D7",
    "Tether USD" to "0xbb5E631c03C65334d1d9EfBCD926DC1265CC20D7",
    "Monero" to "83dm5wyuckG4aPbuMREHCEgLNwVn5i7963SKBhECaA7Ueb7DKBTy639R3QfMtb3DsFHMp8u6WGiCFgbdRDBBcz5sLduUtm8",
    "Zcash" to "t1ZfvNpzfdaW6csT9Kc7iJA7LUU3hmNj2sx",
    "Litecoin" to "LZayhTosZ9ToRvcbeR1gEDgb76Z7ZA2drN",
    "Dash" to "XcTkni8CVAXBcuc5VwvHmsYftVK4CPLetU",
    "Avalanche" to "0xbb5E631c03C65334d1d9EfBCD926DC1265CC20D7",
    "XRP" to "rNpfDm8UwDTumCebchBadjVW2FEPteFgNg",
    "Solana" to "2h6CB3hz5Vb2nYS1RQiXZ4aWTzc5frBPR7Sp1b4muFqb",
    "ADA" to "addr1q8vy2vcp6lacaw8lkc29gufuzajaytc5qc0c2mxlmw5lndxcg5esr4lm36u0lds523cnc9m96gh3gpsls4kdlkaflx6qf6qpvc",
    "Dogecoin" to "DUA4j7mVoc7Rvezu8YgeRKwxNuMzKeDoxD",
    "Tron" to "THWVLGhne5wDsGjd1CNenHDKQGzvGzrzLb",
    "Polkadot" to "1642iaR6AoKyM6qnnMHkfCRfRqRKJ2wC6Cm3UEWEFEz6EtZR",
    "Cosmos" to "cosmos1vt5z6rfj5sgnkdlddkuu8srw3xupyqxscva9hz",
    "Algorand" to "QBOQ6VSLMD77QEF33P5J3HKGOM5RZLNO6P5P3FTWCMQM3ORF6QY2W34KUI",
    "Tezos" to "tz1QUWNYuFqDibGCrwmkdaHSpTx3d6ZdxLMi",
    "Litecoin" to "LZayhTosZ9ToRvcbeR1gEDgb76Z7ZA2drN",
    "Filecoin" to "f1j6pm3chzhgadpf6iwmtux33jb5gccj5arkg4dsq",
)
