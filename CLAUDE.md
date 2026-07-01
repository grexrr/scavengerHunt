# Instructions for working in this repository

## This is a learning project — do not modify application code unless explicitly asked

The user is building this scavenger-hunt game as a thesis project to learn software engineering.
They implement features and fixes themselves. Your default role is investigation, root-cause
analysis, and planning support — not writing or editing `backend/`, `services/`, or frontend
source/test files on their behalf. Only edit code when the user explicitly asks you to make the
change. Reading, tracing, and explaining code is always fine.

## Plan docs must stay in sync with the real codebase

Plans live in `docs/superpowers/plans/`. Whoever executes a task — human or agent — must:

1. **Tick the checkbox (`- [ ] → - [x]`) and update the phase-status table** in the same session
   the corresponding code lands. A checkbox left unchecked after its code is merged is a bug in
   the documentation, not a cosmetic detail.
2. **Never write a plan step that changes a method's implementation, signature, or collaborators
   without also identifying every existing test that exercises or mocks it, and adding a step to
   update those tests.** A task that rewrites `Foo.bar()` but says nothing about `FooTest` (which
   mocks the old collaborator calls) is an incomplete task — the test will silently pass against
   stale expectations or fail with a misleading error.
3. Before marking any task/phase "done," check whether other plan docs reference the same class
   or method (grep across all files in `docs/superpowers/plans/`) and update them too. Stale
   cross-references between plan docs are how this kind of gap slips through.

This rule exists because a real incident happened: `LandmarkManager.getRoundLandmarksIdWithinRadius`
was rewritten to call `GameDataRepository.findByLocationNear` (a MongoDB geospatial query) instead
of `findLandmarkById` + in-memory distance filtering. The plan (Task 5.3 in
`2026-06-13-complete-development-roadmap.md`) documented the production-code change in full but
never mentioned `LandmarkManagerTest.java`, which still mocked the old method. The unstubbed new
call silently returned an empty list from Mockito, and two tests failed with no clue why from the
plan itself.
