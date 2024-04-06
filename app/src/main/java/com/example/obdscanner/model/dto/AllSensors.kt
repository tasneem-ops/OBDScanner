package com.example.obdscanner.model.dto

object AllSensors {
   val allSensors = hashMapOf<Byte, Sensor>()
    fun getSensors(){
        allSensors.put(0x04, Sensor("Calculated engine load", "%", 0x04, {a,b -> a/2.55}))
        allSensors.put(0x05, Sensor("Engine coolant temperature", "°C", 0x05, {a, b -> (a-40).toDouble()}))
        allSensors.put(0x06, Sensor("Short term fuel trim (STFT)—Bank 1", "%", 0x06, {a,b -> a/1.28 - 100}))
        allSensors.put(0x07, Sensor("Long term fuel trim (LTFT)—Bank 1", "%", 0x07, {a,b -> a/1.28 - 100}))
        allSensors.put(0x08, Sensor("Short term fuel trim (STFT)—Bank 2", "%", 0x08, {a,b -> a/1.28 - 100}))
        allSensors.put(0x09, Sensor("Long term fuel trim (LTFT)—Bank 2", "%", 0x09, {a,b -> a/1.28 - 100}))
        allSensors.put(0x0A, Sensor("Fuel pressure", "KPa", 0x0A, {a, b -> (3*a).toDouble()}))
        allSensors.put(0x0B, Sensor("Intake manifold absolute pressure", "KPa", 0x0B, {a, b -> (a).toDouble()}))
        allSensors.put(0x0C, Sensor("Engine speed", "rpm", 0x0C, {a, b -> ((256*a + b)/4).toDouble()}))
        allSensors.put(0x0D, Sensor("Vehicle speed", "km/h", 0x0D, {a, b -> ((256*a + b)/4).toDouble()}))
        allSensors.put(0x0F, Sensor("Intake air temperature", "°C", 0x0F, {a, b -> (a - 40).toDouble()}))
        allSensors.put(0x10, Sensor("Air Flow Rate", "g/s", 0x10, {a, b -> ((256*a + b)/100).toDouble()}))
        allSensors.put(0x11, Sensor("Throttle position", "%", 0x11, {a, b -> (a/ 2.55)}))
    }
}