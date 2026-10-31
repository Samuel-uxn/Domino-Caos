# Dominó Caos

Juego de dominó para 2 a 4 jugadores con poderes y eventos aleatorios, sincronizado en tiempo real con Firebase.

## Configuración de Firebase (una sola vez)
1. Consola de Firebase → **Authentication** → *Sign-in method* → habilitar **Correo/Contraseña**.
2. **Firestore Database** → crear base de datos y pegar estas reglas:
```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /salas/{sala} {
      allow read, write: if request.auth != null;
    }
  }
}
```
3. `app/google-services.json` debe ser el de tu proyecto (ya incluido).

## Arquitectura (MVVM)
| Capa | Paquete | Contenido |
|---|---|---|
| Model | `model` | `Ficha`, `EstadoPartida`, `JugadorEstado`, `Accion`, `Resultado`, `Poder`, `EventoCaos`, `Lado` |
| Lógica | `domain` | `MotorPartida` (reglas completas, sin Android), `ReglasDomino`, `Puntuacion`, `ReglasConfig`, `OpcionesTurno` |
| Datos | `data` | `PartidaRepository` (Firestore, transacciones), `EstadoSerializador` |
| ViewModel | `viewmodel` | `AuthViewModel`, `PartidaViewModel` |
| View | `view` | `Login`, `Registro`, `MenuPrincipal`, `SalaEspera`, `Juego`, `FinPartida` |
| Componentes | `view/componentes` | `FichaView`, `TableroView`, `JugadorInfo`, `PanelPoderes`, `Controles`, `Estilo` |

Flujo: la vista llama al ViewModel → el ViewModel pide al repositorio aplicar una acción del motor dentro de una
transacción de Firestore → todos los dispositivos reciben el nuevo estado por `addSnapshotListener`.

## Pruebas
`./gradlew test` (reglas del dominó, puntuación, motor de la partida, serialización y opciones del turno).
