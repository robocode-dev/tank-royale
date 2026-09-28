using System;
using System.Threading;
using NUnit.Framework;

[assembly: Parallelizable(ParallelScope.None)]
[assembly: LevelOfParallelism(1)]

/// <summary>
/// Bot tasks and MockedServer message handlers block their threads for long periods (bot.Start,
/// intent-continue waits). Leaked ones from earlier tests can exhaust the ThreadPool, which then
/// injects new threads slowly and stalls later tests. A high minimum avoids that ramp-up delay.
/// </summary>
[SetUpFixture]
public class GlobalThreadPoolSetup
{
    [OneTimeSetUp]
    public void RaiseThreadPoolMinimum()
    {
        ThreadPool.GetMinThreads(out var worker, out var io);
        ThreadPool.SetMinThreads(Math.Max(worker, 100), Math.Max(io, 100));
    }
}
