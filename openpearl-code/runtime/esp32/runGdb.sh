xtensa-esp32-elf-gdb -ex "set serial baud 115200" -ex "target remote /dev/ttyUSB0" -ex interrupt $1
