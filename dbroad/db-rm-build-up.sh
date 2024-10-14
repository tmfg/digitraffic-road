#!/bin/bash
docker compose down && docker compose rm db && docker compose build && docker compose up
