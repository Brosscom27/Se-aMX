Manos Inteligentes: App Móvil para el reconocimiento de la Lengua de Señas Mexicana (LSM)

Integrantes:
- Ximena Yahel Juárez Franco
- Mario Cordova Calva
- Roberto Ángel Zamora Ramos

Este proyecto integra un modelo de redes neuronales y aprendizaje profundo a una aplicación móvil desarrollada en Android Studio, con el objetivo de reconocer patrones del abecedario dactilológico de la Lengua de Señas Mexicana.

La cámara del dispositivo captura señas en tiempo real, las cuales son procesadas por un modelo de TensorFlow Lite que reconoce las 27 letras del alfabeto dactilológico. La navegación entre pantallas se implementó usando Jetpack Compose y una barra inferior, permitiendo un flujo intuitivo entre las secciones de inicio, cámara e historial.

La interfaz fue rediseñada con una paleta de colores en tonos azules, tipografía consistente y componentes responsivos, siguiendo principios de UX/UI. Además, se añadió una animación de carga que aparece mientras el modelo procesa la imagen, brindando retroalimentación visual clara. Finalmente, se implementó manejo de errores para garantizar estabilidad durante el reconocimiento y análisis de imágenes.

Requisitos:
- Android Studio
- SDK mínimo: 24 (Android 7.0 Nougat)
- Kotlin 1.9+
- Gradle 8.0+
- Acceso a cámara
- TensorFlow Lite (modelo `lenguajeSenas.tflite` incluido en `/assets`)

Para ejecutar la app:
1. Clona el repositorio:
   ```bash
   git clone https://github.com/Brosscom27/Se-aMX.git
2. Abre el proyecto en Android Studio.
3. Verifica que el emulador o dispositivo físico tenga cámara habilitada.
4. Concede permisos de cámara al iniciar la app.
5. Ejecuta el proyecto (Run > Run 'app').




