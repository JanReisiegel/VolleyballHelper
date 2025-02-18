package com.reisiegel.volleyballhelper.models

enum class ReceiveServeEnum {
    IDEAL, CAN_CONTINUE, TO_OPPONENT_SIDE, CANT_CONTINUE, ERROR
}
enum class ServeEnum {
    ACE, RECEIVED, ERROR
}
enum class AttackEnum{
    HIT,RECEIVED,ERROR, BLOCK
}
enum class BlockEnum {
    ERROR, POINT, NO_POINT
}

enum class SetStates {
    SERVE, RECEIVE, ATTACK_BLOCK, NONE, END_SET
}