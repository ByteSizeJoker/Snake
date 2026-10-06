package com.snake;

/**
 * Allowed directions for the snake.
 */
public enum Direction {
    UP {
        @Override
        public Direction turn(Direction relative) {
            return switch (relative) {
                case LEFT -> LEFT;
                case RIGHT -> RIGHT;
                default -> this;
            };
        }

        @Override
        public Direction opposite() {
            return DOWN;
        }
    },
    DOWN {
        @Override
        public Direction turn(Direction relative) {
            return switch (relative) {
                case LEFT -> RIGHT;
                case RIGHT -> LEFT;
                default -> this;
            };
        }

        @Override
        public Direction opposite() {
            return UP;
        }
    },
    LEFT {
        @Override
        public Direction turn(Direction relative) {
            return switch (relative) {
                case LEFT -> DOWN;
                case RIGHT -> UP;
                default -> this;
            };
        }

        @Override
        public Direction opposite() {
            return RIGHT;
        }
    },
    RIGHT {
        @Override
        public Direction turn(Direction relative) {
            return switch (relative) {
                case LEFT -> UP;
                case RIGHT -> DOWN;
                default -> this;
            };
        }

        @Override
        public Direction opposite() {
            return LEFT;
        }
    };

    public abstract Direction turn(Direction relative);

    public abstract Direction opposite();
}
