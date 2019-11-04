package com.purplekraken.loadguard.compat

class VibrationEffectCompat private constructor(
    internal val timings: LongArray,
    internal val repeat: Int
) {
    companion object {
        fun createWaveForm(timings: LongArray, repeat: Int): VibrationEffectCompat {
            return VibrationEffectCompat(timings, repeat)
        }
    }
}
